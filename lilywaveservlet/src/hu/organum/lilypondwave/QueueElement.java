package hu.organum.lilypondwave;

import javax.servlet.http.HttpServletResponse;

public class QueueElement {

    private String lilypondCode;
    private int scoreSize;
    private HttpServletResponse response;
    
    public QueueElement(String lilypondCode, int scoreSize, HttpServletResponse response) {
        this.lilypondCode = lilypondCode;
        this.scoreSize = scoreSize;
        this.response = response;
    }

    public String getLilypondCode() {
        return lilypondCode;
    }

    public void setLilypondCode(String lilypondCode) {
        this.lilypondCode = lilypondCode;
    }

    public int getScoreSize() {
        return scoreSize;
    }

    public void setScoreSize(int scoreSize) {
        this.scoreSize = scoreSize;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

}
