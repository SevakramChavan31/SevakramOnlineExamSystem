package com.sevak.sevakramonlineexamsystem.dto;

public class OptionResponse {

    private Long id;
    private String optionLabel;
    private String optionText;
    private Boolean correct;

    public OptionResponse(
            Long id,
            String optionLabel,
            String optionText,
            Boolean correct) {

        this.id = id;
        this.optionLabel = optionLabel;
        this.optionText = optionText;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public String getOptionLabel() {
        return optionLabel;
    }

    public String getOptionText() {
        return optionText;
    }

    public Boolean getCorrect() {
        return correct;
    }
}