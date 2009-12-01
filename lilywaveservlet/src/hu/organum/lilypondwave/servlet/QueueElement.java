package hu.organum.lilypondwave.servlet;

import hu.organum.lilypondwave.renderer.Renderer;

import javax.servlet.http.HttpServletResponse;

public class QueueElement {

    private final Renderer renderer;
    private final HttpServletResponse response;

    public QueueElement(Renderer renderer, HttpServletResponse response) {
        this.renderer = renderer;
        this.response = response;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Renderer getRenderer() {
        return renderer;
    }

}
