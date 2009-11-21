package hu.organum.lilypondwave;

@SuppressWarnings("serial")
class RenderingException extends Exception {
    public RenderingException(String message) {
        super(message);
    }

    public RenderingException(Throwable e) {
        super(e);
    }
}