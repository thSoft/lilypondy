package hu.organum.lilypondwave.renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

public class ProcessingCommand {

    private static final Logger LOG = Logger.getLogger(ProcessingCommand.class.getName());
    private final ResultValidator resultValidator;
    private final String template;

    private String getCommand(Map<String, String> replacements) {
    	return Commands.applyReplacements(template, replacements);
    }
    
    private String runProcess(ProcessBuilder processBuilder) throws RenderingException {
        processBuilder.redirectErrorStream(true);
        LOG.info("Starting process:" + processBuilder.command());
        try {
            Process process = processBuilder.start();
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder errorOutputBuilder = new StringBuilder();
            try {
                while ((line = errorStream.readLine()) != null) {
                    errorOutputBuilder.append(line);
                    errorOutputBuilder.append('\n');
                }
            } catch (IOException e) {
                throw new RenderingException(e.getMessage());
            }
            LOG.info("command output:" + errorOutputBuilder.toString());
            process.waitFor();            
            return errorOutputBuilder.toString();
        } catch (IOException e) {
            throw new RenderingException(e);
        } catch (InterruptedException e) {
            throw new RenderingException(e);
        }
    }

    public ProcessingCommand(String template, ResultValidator resultValidator) {
        this.template = template;
        this.resultValidator = resultValidator;
    }

    public void execute(Map<String, String> replacements) throws RenderingException {
		String commandString = getCommand(replacements);
        ProcessBuilder lilypondProcessBuilder;
		if (!isWindows()) {
			lilypondProcessBuilder = new ProcessBuilder("sh", "-c", commandString);
		} else {
			lilypondProcessBuilder = new ProcessBuilder("cmd", "/c", commandString);
		}
        String output = runProcess(lilypondProcessBuilder);
        if (!resultValidator.isOk(replacements)) {
            throw new RenderingException(resultValidator.getMessage(), output);
        }
    }
    
    public static final boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

    
}
