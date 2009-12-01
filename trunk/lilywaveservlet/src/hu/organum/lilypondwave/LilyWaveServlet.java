package hu.organum.lilypondwave;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class LilyWaveServlet.
 * This servlet throws various error codes if something goes wrong:
 * SC_BAD_REQUEST (400) - For errors that should be fixed by the client, ie. change parameters, change the code, make it shorter etc.
 * SC_REQUEST_TIMEOUT (408) - The request could not be handled in the configured time.
 * SC_SERVICE_UNAVAILABLE (503) - If the request should be processed by a different server, eg. the request can not fit in the queue.
 * SC_NOT_FOUND (404) - If the requested rendered file could not be served. 
 */
public class LilyWaveServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LilyWaveServlet.class.getName());

    private static final String PARAM_SOURCE = "q";
    private static final String PARAM_STAFF_SIZE = "s";
    private static final String PARAM_TYPE = "t";
    private static final String PARAM_HASH = "h";

    private static final int DEFAULT_RESOLUTION = 101;
    private static final int DEFAULT_SIZE = 20;
    private final static int MAX_SIZE = 64;

    private Settings settings;

    private BlockingQueue<QueueElement> processingQueue;

    private Map<String, File> cacheIndex;

    /**
     * TODO: take more than one request a time (configurable)
     */
    private class ProcessorWorker implements Runnable {

        @Override
        public void run() {
            LOG.info("Processor worker started");
            while (true) {
                try {
                    QueueElement queueElement = processingQueue.take();
                    synchronized (queueElement) {
                        LOG.info("Code taken from queue, start rendering");
                        renderCode(queueElement.getRenderer(), queueElement.getResponse());
                        LOG.info("rendering finished");
                        queueElement.notifyAll();
                    }
                } catch (InterruptedException e) {
                    LOG.warning(e.getMessage());
                }
            }
        }

    }

    @Override
    public void init() throws ServletException {
        settings = new Settings();
        processingQueue = new ArrayBlockingQueue<QueueElement>(Integer.valueOf(settings.get("QUEUE_CAPACITY")));
        LinkedHashMap<String, File> cashMap = new LinkedHashMap<String, File>(settings.getInteger("CACHE_SIZE"), 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, File> eldest) {
                if (size() > settings.getInteger("CACHE_SIZE")) {
                    eldest.getValue().delete();
                    return true;
                } else {
                    return false;
                }
            }
        };
        cacheIndex = Collections.synchronizedMap(cashMap);
        LOG.info("Servlet initialized");
        new Thread(new ProcessorWorker()).start();
    }

    private Renderer createRenderer(String lilypondCode, int requestedSize) {
        int size;
        if (requestedSize > MAX_SIZE) {
            size = MAX_SIZE;
        } else {
            size = requestedSize;
        }
        int resolution = DEFAULT_RESOLUTION * size / DEFAULT_SIZE;
        Renderer renderer = new Renderer(settings, getUniqueFileName(lilypondCode, size), lilypondCode, resolution);
        return renderer;

    }

    private String getUniqueFileName(String lilypondCode, int size) {
        String digest = null;
        try {
            digest = HexUtil.getHexString(MessageDigest.getInstance("MD5").digest((lilypondCode + size).getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException e) {
            LOG.warning(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LOG.warning(e.getMessage());
        }
        if (digest == null) {
            digest = UUID.randomUUID().toString();
        }
        return digest;
    }

    private void renderCode(Renderer renderer, HttpServletResponse response) {
        File renderingResult = null;
        try {
            renderingResult = renderer.render();
        } catch (RenderingException renderingException) {
            String msg = "Rendering failed:" + renderingException.getMessage() + " verbose message: " + renderingException.getVerboseMessage();
            LOG.severe(msg);
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
        }
        boolean success = false;
        if (renderingResult != null && renderingResult.exists()) {
            cacheIndex.put(renderer.getUniqueName(), renderingResult);
            BufferedInputStream inputStream;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(renderingResult));
                response.setContentType("image/png");
                ServletOutputStream outputStream = response.getOutputStream();
                int b = -1;
                while ((b = inputStream.read()) != -1) {
                    outputStream.write(b);
                }
                outputStream.close();
                success = true;
                if (!success) {
                    try {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } catch (IOException e) {
                        LOG.warning(e.getMessage());
                    }
                }
            } catch (FileNotFoundException e) {
                LOG.warning(e.getMessage());
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
        }
    }

    protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String lilypondCode = request.getParameter(PARAM_SOURCE);
        if (lilypondCode == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else {
            lilypondCode = "\\header { tagline = \"\" }" + lilypondCode;
        }
        int size;
        if (request.getParameter(PARAM_STAFF_SIZE) != null) {
            size = Integer.parseInt(request.getParameter(PARAM_STAFF_SIZE));
        } else {
            size = DEFAULT_SIZE;
        }
        Renderer renderer = createRenderer(lilypondCode, size);
        if (renderer.getAlreadyDone()) {
            renderCode(renderer, response);
        } else {
            QueueElement queueElement = new QueueElement(createRenderer(lilypondCode, size), response);
            try {
                synchronized (queueElement) {
                    boolean insertedInQueue = processingQueue.offer(queueElement);
                    if (!insertedInQueue) {
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                        return;
                    } else {
                        queueElement.wait(settings.getInteger("REQUEST_TIMEOUT"));
                    }
                }
            } catch (InterruptedException e) {
                LOG.warning("Given up waiting for element to be handled");
                response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
            }

        }
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleRequest(request, response);
    }
    

}
