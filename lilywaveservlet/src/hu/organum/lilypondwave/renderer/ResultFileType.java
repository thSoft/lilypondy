package hu.organum.lilypondwave.renderer;

public enum ResultFileType {
	PDF("pdf", "application/pdf"), PNG("png", "image/png"), MIDI("midi", "audio/midi");

	private final String extension;
	private final String mimeType;

	public String getMimeType() {
		return mimeType;
	}

	public String getExtension() {
		return extension;
	}

	private ResultFileType(String extension, String mimeType) {
		this.extension = extension;
		this.mimeType = mimeType;
	}
}
