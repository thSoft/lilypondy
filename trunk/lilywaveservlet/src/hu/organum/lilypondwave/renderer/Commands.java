package hu.organum.lilypondwave.renderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class Commands {

	private static final Logger LOG = Logger.getLogger(Commands.class.getName());
	private static Properties commands;

	static {
		commands = new Properties();
		try {
			commands.load(Commands.class.getResourceAsStream("/commands.properties"));
		} catch (IOException e) {
			LOG.severe(e.getMessage());
		}
	}

	public static ProcessingCommand getCommand(String commandName) {
		String template = commands.getProperty(String.format("command.%s.template", commandName));
		final String resultFileName = commands.getProperty(String.format("command.%s.result.file", commandName));
		final String errorMessage = commands.getProperty(String.format("command.%s.result.error", commandName));
		ResultValidator resultValidator = new ResultValidator() {
			@Override
			public boolean isOk(Map<String, String> replacements) {
				if (resultFileName != null) {
					String changedFileName = applyReplacements(resultFileName, replacements);
					return new File(changedFileName).exists();
				} else {
					return true;
				}
			}
			@Override
			public String getMessage() {
				return errorMessage;
			}
		};
		ProcessingCommand command = new ProcessingCommand(template, resultValidator);
		return command;
	}
	
	public static List<ProcessingCommand> getCommandList(String featureName) {
		List<ProcessingCommand> commandList = new ArrayList<ProcessingCommand>();
		String feature = String.format("feature.%s", featureName);
		String commandListString = commands.getProperty(feature);
		if (commandListString == null) {
			LOG.severe("can't find feature "+feature);
		}
		for (String commandName : commandListString.split(",")) {
			ProcessingCommand command = getCommand(commandName.trim());
			commandList.add(command);
		}
		return commandList;
	}

	public static String applyReplacements(String original, Map<String, String> replacements) {
		String result = original;
		for (Entry<String, String> replacement : replacements.entrySet()) {
			result = result.replaceAll("\\$" + replacement.getKey(), replacement.getValue().replaceAll("\\\\", "\\\\\\\\"));
		}
		return result;

	}
}
