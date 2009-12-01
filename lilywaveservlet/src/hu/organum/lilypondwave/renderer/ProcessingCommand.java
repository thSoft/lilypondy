package hu.organum.lilypondwave.renderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class ProcessingCommand {

    private static final Logger LOG = Logger.getLogger(ProcessingCommand.class.getName());
    private final ResultValidator resultValidator;
    private final String template;

    private String getCommand(Map<String, String> replacements) {
        String command = template;
        for (Entry<String, String> replacement : replacements.entrySet()) {
            command = command.replaceAll("\\$" + replacement.getKey(), replacement.getValue());
        }
        return command;
    }
    
    private String runProcess(ProcessBuilder processBuilder) throws RenderingException {
        processBuilder.redirectErrorStream(true);
        LOG.info("Starting process:" + processBuilder.command());
        try {
            Process process = processBuilder.start();
            process.waitFor();
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
        ProcessBuilder lilypondProcessBuilder = new ProcessBuilder("sh", "-c", commandString);
        String output = runProcess(lilypondProcessBuilder);
        if (!resultValidator.isOk()) {
            throw new RenderingException(resultValidator.getMessage(), output);
        }
    }
    
    
    
}
