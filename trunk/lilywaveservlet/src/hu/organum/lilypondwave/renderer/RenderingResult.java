package hu.organum.lilypondwave.renderer;

import java.io.File;

public class RenderingResult {

	public RenderingResult(File file, String hash, ResultFileType type) {
		super();
		this.file = file;
		this.hash = hash;
		this.type = type;
	}

	private final ResultFileType type;

	public ResultFileType getType() {
		return type;
	}

	public File getFile() {
		return file;
	}

	public String getHash() {
		return hash;
	}

	private final File file;
	private final String hash;

	public boolean exists() {
		return file != null && file.exists();
	}

	public void delete() {
		// TODO delete all files prefixed with the same hash
		if (exists()) {
			file.delete();
		}
	}

}
