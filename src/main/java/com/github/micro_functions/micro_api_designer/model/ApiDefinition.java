package com.github.micro_functions.micro_api_designer.model;

import com.github.microprograms.micro_nested_data_model_sdk.model.NestedEntityDefinition;

public class ApiDefinition {
    private String name;
    private String comment;
    private String description;
    private NestedEntityDefinition requestDefinition;
    private NestedEntityDefinition responseDefinition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NestedEntityDefinition getRequestDefinition() {
        return requestDefinition;
    }

    public void setRequestDefinition(NestedEntityDefinition requestDefinition) {
        this.requestDefinition = requestDefinition;
    }

    public NestedEntityDefinition getResponseDefinition() {
        return responseDefinition;
    }

    public void setResponseDefinition(NestedEntityDefinition responseDefinition) {
        this.responseDefinition = responseDefinition;
    }
}
