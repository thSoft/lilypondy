package hu.organum.lilypondwave.servlet;

import hu.organum.lilypondwave.renderer.Renderer;

import javax.servlet.http.HttpServletResponse;

public class QueueElement {

    private final Renderer renderer;
    private final HttpServletResponse response;
    private final boolean hashOnly;

    public QueueElement(Renderer renderer, HttpServletResponse response, boolean hashOnly) {
        this.renderer = renderer;
        this.response = response;
		this.hashOnly = hashOnly;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Renderer getRenderer() {
        return renderer;
    }
    
    public boolean isHashOnly() {
		return hashOnly;
	}

}
