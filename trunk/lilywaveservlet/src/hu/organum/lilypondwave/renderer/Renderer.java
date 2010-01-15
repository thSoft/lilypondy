package hu.organum.lilypondwave.renderer;

import hu.organum.lilypondwave.common.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Renderer {

	private File baseDir;
	private File jailedBaseDir;
	private final String uniqueName;
	private final String lilypondCode;
	private final Integer resolution;
	private final Settings settings;

	private File jailDir;
	private final String featureName;

	private Map<String, String> getReplacements() {
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("lilypond", settings.get("LILYPOND_COMMAND"));
		replacements.put("gs", settings.get("GS_COMMAND"));
		replacements.put("convert", settings.get("CONVERT_COMMAND"));
		replacements.put("user", settings.get("USER"));
		replacements.put("group", settings.get("GROUP"));
		replacements.put("jail", jailDir.getPath());
		replacements.put("dir", baseDir.getPath());
		replacements.put("jailedBase", jailedBaseDir.getPath());
		if (resolution != null) {
			replacements.put("resolution", resolution.toString());
		}
		replacements.put("hash", uniqueName);
		return replacements;
	}

	private void renderLilyPondCode(List<ProcessingCommand> commands) throws RenderingException {
		File tempFile = new File(jailedBaseDir, uniqueName + ".ly");
		Writer fileWriter;
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8");
			fileWriter.write(lilypondCode);
			fileWriter.close();
		} catch (IOException e) {
			throw new RenderingException(e);
		}
		try {
			Map<String, String> replacements = getReplacements();
			for (ProcessingCommand command : commands) {
				command.execute(replacements);
			}
		} finally {
			tempFile.delete();
		}
	}

	public Renderer(Settings settings, String uniqueName, String lilypondCode, String featureName, int resolution) {
		this.settings = settings;
		this.uniqueName = uniqueName;
		this.lilypondCode = lilypondCode;
		this.featureName = featureName;
		this.resolution = resolution;
		this.baseDir = new File(settings.get("DIR"));
		this.jailedBaseDir = new File(settings.get("JAIL") + settings.get("DIR"));
		this.jailDir = new File(settings.get("JAIL"));
	}
	
	public File getResultFile(ResultFileType resultFileType) {
		return new File(jailDir, uniqueName + "." + resultFileType.getExtension());
	}

	public File getResultFile() {
		ResultFileType resultFileType = Commands.getResultFileType(featureName);
		return getResultFile(resultFileType);
	}

	/**
	 * Returns the file created as the result of the rendering.
	 * 
	 * @return
	 */
	public RenderingResult render() throws RenderingException {
		try {
			File resultFile = getResultFile();
			if (!getAlreadyDone()) {
				renderLilyPondCode(Commands.getCommandList(featureName));
			}
			RenderingResult result = new RenderingResult(resultFile, uniqueName, Commands.getResultFileType(featureName));
			return result;
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new RenderingException(e);
		}
	}

	public boolean getAlreadyDone() {
		return getResultFile().length() > 0;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public boolean resultExists(ResultFileType resultFileType) {
		return getResultFile(resultFileType).length() > 0;
	}

}
