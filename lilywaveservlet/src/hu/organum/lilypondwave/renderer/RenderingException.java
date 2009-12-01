package hu.organum.lilypondwave.renderer;

public class RenderingException extends Exception {

    private String verboseMessage;
    
    public RenderingException(String message) {
        this(message, "");
    }

    public RenderingException(String message, String verboseMessage) {
        super(message);
        this.verboseMessage = verboseMessage;
        
    }

    public RenderingException(Throwable e) {
        this(e, "");
    }

    public RenderingException(Throwable e, String verboseMessage) {
        super(e);
        this.verboseMessage = verboseMessage;
    }

    public String getVerboseMessage() {
        return verboseMessage;
    }

    public void setVerboseMessage(String verboseMessage) {
        this.verboseMessage = verboseMessage;
    }

}