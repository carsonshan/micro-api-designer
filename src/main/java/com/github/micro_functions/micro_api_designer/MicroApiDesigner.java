package com.github.micro_functions.micro_api_designer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.micro_functions.micro_api_designer.model.EngineDefinition;
import com.github.micro_functions.micro_api_designer.model.MixinDefinition;

public class MicroApiDesigner {
    private static final Charset encoding = Charset.forName("utf8");

    public static EngineDefinition buildEngineDefinition(String engineConfigFilePath) throws IOException {
        String json = readFile(engineConfigFilePath);
        return parseMixin(JSON.parseObject(json, EngineDefinition.class));
    }

    private static String readFile(String file) throws IOException {
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            List<String> lines = IOUtils.readLines(input, encoding);
            return StringUtils.join(lines, "");
        } catch (IOException e) {
            throw e;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    private static EngineDefinition parseMixin(EngineDefinition engineDefinition) throws IOException {
        JSONObject root = (JSONObject) JSON.toJSON(engineDefinition);
        List<MixinDefinition> mixinDefinitions = engineDefinition.getMixinDefinitions();
        if (mixinDefinitions != null && !mixinDefinitions.isEmpty()) {
            for (MixinDefinition x : mixinDefinitions) {
                String sourceString = x.getSource();
                String sourceFilePath = sourceString.substring(0, sourceString.lastIndexOf("#"));
                String sourceLocation = sourceString.substring(sourceString.lastIndexOf("#") + 1);
                JSONObject sourceJson = JSON.parseObject(readFile(sourceFilePath));
                Object source = getObjectByLocation(sourceJson, sourceLocation);
                String targetLocation = x.getTarget();
                Object target = getObjectByLocation(root, targetLocation);
                mixin(source, target);
            }
        }
        return JSON.toJavaObject(root, EngineDefinition.class);
    }

    private static Object getObjectByLocation(JSONObject jsonObject, String location) {
        if (StringUtils.isBlank(location)) {
            return jsonObject;
        }
        int indexOfDot = location.indexOf(".");
        if (indexOfDot == -1) {
            return jsonObject.get(location);
        }
        String key = location.substring(0, indexOfDot);
        String remainingKey = location.substring(indexOfDot + 1);
        return getObjectByLocation(jsonObject.getJSONObject(key), remainingKey);
    }

    private static void mixin(Object source, Object target) {
        if (target instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) target;
            jsonObject.putAll((JSONObject) source);
        }
        if (target instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) target;
            jsonArray.addAll((JSONArray) source);
        }
    }
}
