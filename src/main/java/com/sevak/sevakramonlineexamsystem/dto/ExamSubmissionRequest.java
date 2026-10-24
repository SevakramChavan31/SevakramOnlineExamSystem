package com.sevak.sevakramonlineexamsystem.dto;

import java.util.Map;

public class ExamSubmissionRequest {

    private Long attemptId;

    private Map<Long, Long> answers;

    public ExamSubmissionRequest() {
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Map<Long, Long> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, Long> answers) {
        this.answers = answers;
    }
}