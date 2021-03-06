package com.github.micro_functions.micro_api_designer.model;

import java.util.List;

import com.github.microprograms.micro_relational_data_model_sdk.model.PlainEntityDefinition;

public class EngineDefinition {
    private String comment;
    private String description;
    private String version;
    private List<ApiDefinition> apiDefinitions;
    private List<ErrorCodeDefinition> errorCodeDefinitions;
    private List<PlainEntityDefinition> modelDefinitions;
    private ServerAddressDefinition serverAddressDefinition;
    private ShowdocDefinition showdocDefinition;
    private List<MixinDefinition> mixinDefinitions;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<ApiDefinition> getApiDefinitions() {
        return apiDefinitions;
    }

    public void setApiDefinitions(List<ApiDefinition> apiDefinitions) {
        this.apiDefinitions = apiDefinitions;
    }

    public List<ErrorCodeDefinition> getErrorCodeDefinitions() {
        return errorCodeDefinitions;
    }

    public void setErrorCodeDefinitions(List<ErrorCodeDefinition> errorCodeDefinitions) {
        this.errorCodeDefinitions = errorCodeDefinitions;
    }

    public List<PlainEntityDefinition> getModelDefinitions() {
        return modelDefinitions;
    }

    public void setModelDefinitions(List<PlainEntityDefinition> modelDefinitions) {
        this.modelDefinitions = modelDefinitions;
    }

    public ServerAddressDefinition getServerAddressDefinition() {
        return serverAddressDefinition;
    }

    public void setServerAddressDefinition(ServerAddressDefinition serverAddressDefinition) {
        this.serverAddressDefinition = serverAddressDefinition;
    }

    public ShowdocDefinition getShowdocDefinition() {
        return showdocDefinition;
    }

    public void setShowdocDefinition(ShowdocDefinition showdocDefinition) {
        this.showdocDefinition = showdocDefinition;
    }

    public List<MixinDefinition> getMixinDefinitions() {
        return mixinDefinitions;
    }

    public void setMixinDefinitions(List<MixinDefinition> mixinDefinitions) {
        this.mixinDefinitions = mixinDefinitions;
    }
}
