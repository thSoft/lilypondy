package hu.organum.lilypondwave.renderer;

public class RendererConfiguration {

    private String uniqueName;
    private String lilypondCode;
    private String featureName;
    private Integer resolution;

    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public String getLilypondCode() {
        return lilypondCode;
    }

    public void setLilypondCode(String lilypondCode) {
        this.lilypondCode = lilypondCode;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public Integer getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

}
