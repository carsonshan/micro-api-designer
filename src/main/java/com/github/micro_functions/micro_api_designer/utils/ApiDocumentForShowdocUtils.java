package com.github.micro_functions.micro_api_designer.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.micro_functions.micro_api_core.enums.MicroApiReserveResponseCodeEnum;
import com.github.micro_functions.micro_api_designer.model.ApiDefinition;
import com.github.micro_functions.micro_api_designer.model.EngineDefinition;
import com.github.micro_functions.micro_api_designer.model.ErrorCodeDefinition;
import com.github.micro_functions.micro_api_designer.model.ServerAddressDefinition;
import com.github.micro_functions.micro_api_designer.model.ShowdocDefinition;
import com.github.microprograms.micro_nested_data_model_sdk.model.NestedEntityDefinition;
import com.github.microprograms.micro_nested_data_model_sdk.model.NestedFieldDefinition;
import com.github.microprograms.micro_relational_data_model_sdk.model.PlainEntityDefinition;
import com.github.microprograms.micro_relational_data_model_sdk.model.PlainFieldDefinition;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;

public class ApiDocumentForShowdocUtils {

    public static void update(EngineDefinition engineDefinition) throws IOException {
        _updateHomePage(engineDefinition);
        _updateErrorCodePage(engineDefinition);
        _updateEntityDefinitionPage(engineDefinition);
        _updateApiPages(engineDefinition);
    }

    private static void _updatePage(String catName, String pageTitle, String pageContent, int sNumber, ShowdocDefinition showdocDefinition) throws IOException {
        Map<String, String> req = new HashMap<>();
        req.put("api_key", showdocDefinition.getApiKey());
        req.put("api_token", showdocDefinition.getApiToken());
        req.put("cat_name", catName);
        req.put("cat_name_sub", "");
        req.put("page_title", pageTitle);
        req.put("page_content", pageContent);
        req.put("s_number", String.valueOf(sNumber));
        String response = new JdkRequest(showdocDefinition.getUrl()).body().formParams(req).back().method(Request.POST).fetch().body();
        JSONObject resp = JSON.parseObject(response);
        if (resp.getIntValue("error_code") != 0) {
            throw new RuntimeException(response);
        }
    }

    private static void _updateHomePage(EngineDefinition engineDefinition) throws IOException {
        _updatePage("", "说明", _buildMarkdownForHomePage(engineDefinition), 1, engineDefinition.getShowdocDefinition());
    }

    private static void _updateErrorCodePage(EngineDefinition engineDefinition) throws IOException {
        _appendCommonErrorCodeDefinitions(engineDefinition);
        _updatePage("", "全局错误码", _buildMarkdownForErrorCode(engineDefinition), 2, engineDefinition.getShowdocDefinition());
    }

    private static void _updateEntityDefinitionPage(EngineDefinition engineDefinition) throws IOException {
        _updatePage("", "实体定义", _buildMarkdownForEntityDefinition(engineDefinition), 3, engineDefinition.getShowdocDefinition());
    }

    private static void _updateApiPages(EngineDefinition engineDefinition) throws IOException {
        List<ApiDefinition> apiDefinitions = engineDefinition.getApiDefinitions();
        for (int i = 0; i < apiDefinitions.size(); i++) {
            ApiDefinition apiDefinition = apiDefinitions.get(i);
            String comment = apiDefinition.getComment();
            String catName = comment.indexOf('-') == -1 ? "" : comment.replaceFirst("\\s*-.*$", "");
            String pageTitle = comment.replaceFirst("^.*-\\s*", "");
            String pageContent = _buildMarkdownForApi(apiDefinition, engineDefinition);
            _updatePage(catName, pageTitle, pageContent, 100 + i, engineDefinition.getShowdocDefinition());
        }
    }

    private static String _buildMarkdownForHomePage(EngineDefinition engineDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("#### ").append(engineDefinition.getComment()).append("\n");
        sb.append("# ").append(engineDefinition.getVersion()).append("\n");
        sb.append("GenerateBy [MicroApiDesigner](https://github.com/micro-functions/micro-api-designer)").append(" ").append(_getTime()).append("\n");
        return sb.toString();
    }

    private static String _getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private static String _buildMarkdownForErrorCode(EngineDefinition engineDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("|错误码|错误解释|").append("\n");
        sb.append("|-----|-----|").append("\n");
        for (ErrorCodeDefinition x : engineDefinition.getErrorCodeDefinitions()) {
            sb.append("|").append(x.getCode()).append("|").append(x.getMessage()).append("|").append("\n");
        }
        return sb.toString();
    }

    private static String _buildMarkdownForEntityDefinition(EngineDefinition engineDefinition) {
        StringBuffer sb = new StringBuffer();
        for (PlainEntityDefinition entityDefinition : engineDefinition.getModelDefinitions()) {
            sb.append(String.format("**%s（%s）**", entityDefinition.getComment(), entityDefinition.getJavaClassName())).append("\n\n");
            sb.append("|字段名|类型|说明|").append("\n");
            sb.append("|-----|-----|-----|").append("\n");
            for (PlainFieldDefinition x : entityDefinition.getFieldDefinitions()) {
                sb.append("|").append(x.getName()).append("|").append(_getType(x.getJavaType())).append("|").append(x.getComment()).append("\n");
            }
        }
        return sb.toString();
    }

    private static String _buildMarkdownForApi(ApiDefinition apiDefinition, EngineDefinition engineDefinition) {
        StringBuffer sb = new StringBuffer();
        sb.append("**简要描述：**").append("\n\n");
        sb.append("- ").append(apiDefinition.getComment()).append("\n\n");
        if (StringUtils.isNoneBlank(apiDefinition.getDescription())) {
            sb.append("**详细描述：**").append("\n\n");
            sb.append("- ").append(apiDefinition.getDescription()).append("\n\n");
        }
        sb.append("**请求URL：**").append("\n\n");
        sb.append("- ` ").append(_buildApiUrl(apiDefinition, engineDefinition)).append(" `").append("\n\n");
        sb.append("**请求方式：**").append("\n\n");
        sb.append("- POST").append("\n\n");
        sb.append("**参数：**").append("\n\n");
        sb.append("|参数名|必选|类型|说明|").append("\n");
        sb.append("|-----|-----|-----|-----|").append("\n");
        _appendCommonRequestFieldDefinitions(apiDefinition);
        for (NestedFieldDefinition x : apiDefinition.getRequestDefinition().getFieldDefinitions()) {
            sb.append("|").append(x.getName()).append("|").append(x.getRequired() ? "是" : "否").append("|").append(_getType(x.getJavaType())).append("|").append(x.getComment()).append("\n");
        }
        sb.append("**参数示例**").append("\n\n");
        sb.append("```").append("\n");
        sb.append(JsonPrettyPrinter.format(_buildExampleInJson(apiDefinition.getRequestDefinition()).toJSONString())).append("\n");
        sb.append("```").append("\n\n");
        sb.append("**返回参数说明**").append("\n\n");
        sb.append("|参数名|类型|说明|").append("\n");
        sb.append("|-----|-----|-----|").append("\n");
        _appendCommonResponseFieldDefinitions(apiDefinition);
        for (NestedFieldDefinition x : apiDefinition.getResponseDefinition().getFieldDefinitions()) {
            sb.append("|").append(x.getName()).append("|").append(_getType(x.getJavaType())).append("|").append(x.getComment()).append("\n");
        }
        sb.append("**返回示例**").append("\n\n");
        sb.append("```").append("\n");
        sb.append(JsonPrettyPrinter.format(_buildExampleInJson(apiDefinition.getResponseDefinition()).toJSONString())).append("\n");
        sb.append("```").append("\n\n");
        sb.append("**备注**").append("\n\n");
        sb.append("- 更多返回错误代码请看首页的错误代码描述").append("\n\n");
        return sb.toString();
    }

    private static String _buildApiUrl(ApiDefinition apiDefinition, EngineDefinition engineDefinition) {
        ServerAddressDefinition x = engineDefinition.getServerAddressDefinition();
        return String.format("http://%s:%s%s%s", x.getHost(), x.getPort(), x.getUrl(), apiDefinition.getName());
    }

    private static void _appendCommonRequestFieldDefinitions(ApiDefinition apiDefinition) {
        List<NestedFieldDefinition> commonFieldDefinitions = new ArrayList<>();
        // commonFieldDefinitions.add(_buildFieldDefinition("【接口名】<br/>" +
        // apiDefinition.getName(), "apiName", "String", true,
        // apiDefinition.getName()));
        if (apiDefinition.getRequestDefinition() == null) {
            NestedEntityDefinition requestDefinition = new NestedEntityDefinition();
            requestDefinition.setFieldDefinitions(commonFieldDefinitions);
            apiDefinition.setRequestDefinition(requestDefinition);
        } else {
            apiDefinition.getRequestDefinition().getFieldDefinitions().addAll(0, commonFieldDefinitions);
        }
    }

    private static void _appendCommonErrorCodeDefinitions(EngineDefinition engineDefinition) {
        if (engineDefinition.getErrorCodeDefinitions() == null) {
            engineDefinition.setErrorCodeDefinitions(new ArrayList<>());
        }
        List<ErrorCodeDefinition> list = new ArrayList<>();
        for (MicroApiReserveResponseCodeEnum x : MicroApiReserveResponseCodeEnum.values()) {
            ErrorCodeDefinition errorCodeDefinition = new ErrorCodeDefinition();
            errorCodeDefinition.setCode(x.getCode());
            errorCodeDefinition.setMessage("系统 - " + x.getMessage());
            list.add(errorCodeDefinition);
        }
        engineDefinition.getErrorCodeDefinitions().addAll(0, list);
    }

    private static void _appendCommonResponseFieldDefinitions(ApiDefinition apiDefinition) {
        List<NestedFieldDefinition> commonFieldDefinitions = new ArrayList<>();
        commonFieldDefinitions.add(_buildFieldDefinition("错误码(0正常,非0错误)", "code", "Integer", true, MicroApiReserveResponseCodeEnum.success.getCode()));
        commonFieldDefinitions.add(_buildFieldDefinition("错误提示", "msg", "String", true, MicroApiReserveResponseCodeEnum.success.getMessage()));
        if (apiDefinition.getResponseDefinition() == null) {
            NestedEntityDefinition responseDefinition = new NestedEntityDefinition();
            responseDefinition.setFieldDefinitions(commonFieldDefinitions);
            apiDefinition.setResponseDefinition(responseDefinition);
        } else {
            apiDefinition.getResponseDefinition().getFieldDefinitions().addAll(0, commonFieldDefinitions);
        }
    }

    private static NestedFieldDefinition _buildFieldDefinition(String comment, String name, String javaType, boolean required, Object example) {
        NestedFieldDefinition fieldDefinition = new NestedFieldDefinition();
        fieldDefinition.setComment(comment);
        fieldDefinition.setJavaType(javaType);
        fieldDefinition.setName(name);
        fieldDefinition.setRequired(required);
        fieldDefinition.setExample(example);
        return fieldDefinition;
    }

    private static String _getType(String javaType) {
        switch (javaType) {
        case "Integer":
            return "int";
        case "Long":
            return "long";
        case "String":
            return "string";
        default:
            return javaType.replaceFirst(".*\\.", "").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
    }

    private static JSONObject _buildExampleInJson(NestedEntityDefinition entityDefinition) {
        JSONObject json = new JSONObject(16, true);
        for (NestedFieldDefinition fieldDefinition : entityDefinition.getFieldDefinitions()) {
            json.put(fieldDefinition.getName(), fieldDefinition.getExample());
        }
        return json;
    }
}
