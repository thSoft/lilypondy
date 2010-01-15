package hu.organum.lilypondwave.servlet;

import hu.organum.lilypondwave.renderer.Renderer;
import hu.organum.lilypondwave.renderer.ResultFileType;

import javax.servlet.http.HttpServletResponse;

public class QueueElement {

    private final Renderer renderer;
    private final HttpServletResponse response;
    private final boolean hashOnly;
	private final ResultFileType resultFileType;

    public ResultFileType getResultFileType() {
		return resultFileType;
	}

	public QueueElement(Renderer renderer, HttpServletResponse response, boolean hashOnly, ResultFileType resultFileType) {
        this.renderer = renderer;
        this.response = response;
		this.hashOnly = hashOnly;
		this.resultFileType = resultFileType;
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
