package hu.organum.lilypondwave;

class RenderingException extends Exception {

    public RenderingException(String message) {
        super(message);
    }

    public RenderingException(Throwable e) {
        super(e);
    }

}