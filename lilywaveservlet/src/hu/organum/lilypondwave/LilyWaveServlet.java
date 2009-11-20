package hu.organum.lilypondwave;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * Servlet implementation class LilyWaveServlet
 */
public class LilyWaveServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LilyWaveServlet.class.getName());

    private final static String PARAM_SOURCE = "q";
    private final static String PARAM_STAFF_SIZE = "s";

    private final static int DEFAULT_RESOLUTION = 101;
    private final static int DEFAULT_SIZE = 16;

    private Settings settings;

    private BlockingQueue<QueueElement> processingQueue;

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
                        LOG.info("Code taken from queue");
                        String lilypondCode = "\\header { tagline = \"\" }" + queueElement.getLilypondCode();
                        int resolution = DEFAULT_RESOLUTION;
                        int size = queueElement.getScoreSize();
                        if (size > 64) {
                            size = 64;
                        }
                        resolution = DEFAULT_RESOLUTION * size / DEFAULT_SIZE;
                        LOG.info("start rendering");
                        renderCode(lilypondCode, resolution, queueElement.getResponse());
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
        LOG.info("Servlet initialized");
        new Thread(new ProcessorWorker()).start();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lilypondCode = request.getParameter(PARAM_SOURCE);
        if (lilypondCode == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int size;
        if (request.getParameter(PARAM_STAFF_SIZE) != null) {
            size = Integer.parseInt(request.getParameter(PARAM_STAFF_SIZE));
        } else {
            size = DEFAULT_SIZE;
        }
        QueueElement queueElement = new QueueElement(lilypondCode, size, response);
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

    private String getUniqueFileName() {
        return UUID.randomUUID().toString();
    }

    private void renderCode(String lilypondCode, int resolution, HttpServletResponse response) {
        File renderingResult = new Renderer(settings, getUniqueFileName(), lilypondCode, resolution).render();
        boolean success = false;
        if (renderingResult != null) {
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
            } catch (FileNotFoundException e) {
                LOG.warning(e.getMessage());
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
        }
        if (!success) {
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                LOG.warning(e.getMessage());
            }
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}
