package hu.organum.lilypondwave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Renderer {

    private static final Logger LOG = Logger.getLogger(Renderer.class.getName());

    private final static String LILYPOND_COMMAND_TEMPLATE = "%s -fps -dbackend=eps -ddelete-intermediate-files -j%s,%s,%s,%s %s";
    private final static String GS_COMMAND_TEMPLATE = "chroot %s %s -dEPSCrop -dGraphicsAlphaBits=4 -dTextAlphaBits=4 -dNOPAUSE -sDEVICE=png16m -sOutputFile=\"%s.png\" -r%d \"%s-1.eps\" -c quit";
    private final static String CONVERT_COMMAND_TEMPLATE = "chroot %s %s -trim %s %s";
    private final static String DELETE_COMMAND_TEMPLATE = "chroot %s sh -c \"cd %s && rm -f *.tex *.texi *.count *.eps *.ly\"";

    private File baseDir;
    private File jailedBaseDir;
    private final String uniqueName;
    private final String lilypondCode;
    private final int resolution;
    private final Settings settings;

    private String getLilypondCommand(String renderedFileName) {
        return String.format(LILYPOND_COMMAND_TEMPLATE, settings.get("LILYPOND_COMMAND"), settings.get("USER"), settings.get("GROUP"), settings
                .get("JAIL"), settings.get("DIR"), renderedFileName);
    }

    private String getGhostscriptCommand(String renderedFileNameWithoutExtension, int resolution) {
        return String.format(GS_COMMAND_TEMPLATE, settings.get("JAIL"), settings.get("GS_COMMAND"), renderedFileNameWithoutExtension, resolution,
                renderedFileNameWithoutExtension);
    }

    private String getConvertCommand(String pngName) {
        return String.format(CONVERT_COMMAND_TEMPLATE, settings.get("JAIL"), settings.get("CONVERT_COMMAND"), pngName, pngName);
    }

    private void runProcess(ProcessBuilder processBuilder) throws RenderingException {
        processBuilder.redirectErrorStream(true);
        LOG.info("Starting process:" + processBuilder.command());
        try {
            Process process = processBuilder.start();
            process.waitFor();
            BufferedReader is = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder sb = new StringBuilder();
            try {
                while ((line = is.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                throw new RenderingException(e.getMessage());
            }
            LOG.info("command output:" + sb.toString());
        } catch (IOException e) {
            throw new RenderingException(e);
        } catch (InterruptedException e) {
            throw new RenderingException(e);
        }
    }

    private void renderLilyPond() throws RenderingException {
        File tempFile = new File(jailedBaseDir, uniqueName + ".ly");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(tempFile);
            fileWriter.write(lilypondCode);
            fileWriter.close();
        } catch (IOException e) {
            throw new RenderingException(e);
        }
        String lilypondCommand = getLilypondCommand(uniqueName + ".ly");
        ProcessBuilder lilypondProcessBuilder = new ProcessBuilder("sh", "-c", lilypondCommand);
        runProcess(lilypondProcessBuilder);
        tempFile.delete();
    }

    private void renderEps() throws RenderingException {
        File epsFile = new File(jailedBaseDir, uniqueName + "-1.eps");
        if (epsFile.exists()) {
            String gsCommand = getGhostscriptCommand(baseDir.getAbsolutePath() + "/" + uniqueName, resolution);
            ProcessBuilder gsProcessBuilder = new ProcessBuilder("sh", "-c", gsCommand);
            gsProcessBuilder.redirectErrorStream(true);
            runProcess(gsProcessBuilder);
        } else {
            throw new RenderingException("EPS file doesn't exist");
        }
    }

    private void cropPng() throws RenderingException {
        File pngFile = new File(jailedBaseDir, uniqueName + ".png");
        if (pngFile.exists()) {
            ProcessBuilder convertProcessBuilder = new ProcessBuilder();
            String convertCommand = getConvertCommand(baseDir.getAbsolutePath() + "/" + uniqueName + ".png");
            convertProcessBuilder.command("sh", "-c", convertCommand);
            runProcess(convertProcessBuilder);
        } else {
            throw new RenderingException(pngFile.getAbsolutePath() + " doesn't exist");
        }
    }

    public Renderer(Settings settings, String uniqueName, String lilypondCode, int resolution) {
        this.settings = settings;
        this.uniqueName = uniqueName;
        this.lilypondCode = lilypondCode;
        this.resolution = resolution;
        this.baseDir = new File(settings.get("DIR"));
        this.jailedBaseDir = new File(settings.get("JAIL") + settings.get("DIR"));
    }

    private void deleteTemporaryFiles() throws RenderingException {
        String deleteCommand = String.format(DELETE_COMMAND_TEMPLATE, settings.get("JAIL"), settings.get("DIR"));
        ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", deleteCommand);
        runProcess(processBuilder);
    }

    public File getPngFile() {
        return new File(jailedBaseDir, uniqueName + ".png");
    }
    
    /**
     * Returns the file created as the result of the rendering.
     * @return
     */
    public File render() {
        File result = null;
        File pngFile = getPngFile();
        if (getAlreadyDone()) {
            result = pngFile;
        } else {
            try {
                renderLilyPond();
                renderEps();
                cropPng();
                deleteTemporaryFiles();
                result = pngFile;
            } catch (RenderingException e) {
                LOG.severe("Rendering failed:" + e.getMessage());
            }
        }
        return result;
    }

    public boolean getAlreadyDone() {
        return getPngFile().length() > 0;
    }

    public String getUniqueName() {
        return uniqueName;
    }

}
