package ru.ifmo.movies_app.dto;

public class PersonReassignmentDto {

    private Long directorReplacementId;
    private Long screenwriterReplacementId;
    private Long operatorReplacementId;

    public Long getDirectorReplacementId() {
        return directorReplacementId;
    }

    public void setDirectorReplacementId(Long directorReplacementId) {
        this.directorReplacementId = directorReplacementId;
    }

    public Long getScreenwriterReplacementId() {
        return screenwriterReplacementId;
    }

    public void setScreenwriterReplacementId(Long screenwriterReplacementId) {
        this.screenwriterReplacementId = screenwriterReplacementId;
    }

    public Long getOperatorReplacementId() {
        return operatorReplacementId;
    }

    public void setOperatorReplacementId(Long operatorReplacementId) {
        this.operatorReplacementId = operatorReplacementId;
    }
}
