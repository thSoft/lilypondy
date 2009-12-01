package hu.organum.lilypondwave;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {

    private final static String LILYPOND_COMMAND_TEMPLATE = "$lilypond -fps -dbackend=eps -ddelete-intermediate-files -j$user,$group,$jail,$dir $ly";
    private final static String GS_COMMAND_TEMPLATE = "chroot $jail $gs -dEPSCrop -dGraphicsAlphaBits=4 -dTextAlphaBits=4 -dNOPAUSE -sDEVICE=png16m -sOutputFile=\"/$hash.png\" -r$resolution \"$dir/$hash-1.eps\" -c quit";
    private final static String CONVERT_COMMAND_TEMPLATE = "chroot $jail $convert -trim $png $png";
    private final static String DELETE_COMMAND_TEMPLATE = "chroot $jail sh -c \"cd $dir && rm -f *.tex *.texi *.count *.eps *.ly\"";

    private File baseDir;
    private File jailedBaseDir;
    private final String uniqueName;
    private final String lilypondCode;
    private final Integer resolution;
    private final Settings settings;

    private File jailDir;

    private Map<String, String> getReplacements() {
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("lilypond", settings.get("LILYPOND_COMMAND"));
        replacements.put("gs", settings.get("GS_COMMAND"));
        replacements.put("convert", settings.get("CONVERT_COMMAND"));
        replacements.put("user", settings.get("USER"));
        replacements.put("group", settings.get("GROUP"));
        replacements.put("jail", jailDir.getPath());
        replacements.put("dir", baseDir.getPath());

        replacements.put("resolution", resolution.toString());
        replacements.put("hash", uniqueName);
        replacements.put("ly", uniqueName + ".ly");
        replacements.put("png", uniqueName + ".png");
        return replacements;
    }

    private void renderCroppedPng() throws RenderingException {
        List<ProcessingCommand> commands = new ArrayList<ProcessingCommand>();
        commands.add(new ProcessingCommand(LILYPOND_COMMAND_TEMPLATE, new ResultValidator() {
            @Override
            public boolean isOk() {
                return getEpsFile().exists();
            }
            @Override
            public String getMessage() {
                return "Lilypond processing failed";
            }
        }));

        commands.add(new ProcessingCommand(GS_COMMAND_TEMPLATE, new ResultValidator() {
            @Override
            public boolean isOk() {
                return getPngFile().exists();
            }

            @Override
            public String getMessage() {
                return "Ghostscript processing failed";
            }
        }));
        
        commands.add(new ProcessingCommand(CONVERT_COMMAND_TEMPLATE, new ResultValidator()));
        commands.add(new ProcessingCommand(DELETE_COMMAND_TEMPLATE, new ResultValidator()));
        
        Map<String, String> replacements = getReplacements();
        for (ProcessingCommand command : commands) {
            command.execute(replacements);
        }
    }

    private File getEpsFile() {
        return new File(jailedBaseDir, uniqueName + "-1.eps");
    }

    private void renderLilyPondCode() throws RenderingException {
        File tempFile = new File(jailedBaseDir, uniqueName + ".ly");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(tempFile);
            fileWriter.write(lilypondCode);
            fileWriter.close();
        } catch (IOException e) {
            throw new RenderingException(e);
        }
        try {
            renderCroppedPng();
        } finally {
            tempFile.delete();
        }
    }

    public Renderer(Settings settings, String uniqueName, String lilypondCode, int resolution) {
        this.settings = settings;
        this.uniqueName = uniqueName;
        this.lilypondCode = lilypondCode;
        this.resolution = resolution;
        this.baseDir = new File(settings.get("DIR"));
        this.jailedBaseDir = new File(settings.get("JAIL") + settings.get("DIR"));
        this.jailDir = new File(settings.get("JAIL"));
    }

    public File getPngFile() {
        return new File(jailDir, uniqueName + ".png");
    }

    /**
     * Returns the file created as the result of the rendering.
     * @return
     */
    public File render() throws RenderingException {
        File result = null;
        File pngFile = getPngFile();
        if (getAlreadyDone()) {
            result = pngFile;
        } else {
            renderLilyPondCode();
            result = pngFile;
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
