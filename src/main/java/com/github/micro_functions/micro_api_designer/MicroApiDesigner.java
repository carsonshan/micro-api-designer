package com.github.micro_functions.micro_api_designer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.micro_functions.micro_api_core.model.ResponseCode;
import com.github.micro_functions.micro_api_designer.model.EngineDefinition;
import com.github.micro_functions.micro_api_designer.model.ErrorCodeDefinition;
import com.github.micro_functions.micro_api_designer.model.MixinDefinition;
import com.github.microprograms.micro_relational_data_model_sdk.utils.JavaParserUtils;

public class MicroApiDesigner {
    private static final Charset encoding = Charset.forName("utf8");
    public static final String error_code_enum_class_name = "ErrorCodeEnum";

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

    public static void updateErrorCodeJavaFile(String srcFolder, String javaPackageName, EngineDefinition engineDefinition) throws IOException {
        File javaFile = JavaParserUtils.buildJavaSourceFile(srcFolder, javaPackageName, error_code_enum_class_name);
        CompilationUnit cu = null;
        if (javaFile.exists()) {
            cu = JavaParser.parse(javaFile, encoding);
            EnumDeclaration errorCodeEnumDeclaration = cu.getEnumByName(error_code_enum_class_name).get();
            _deleteErrorCodeEnumDeclaration(errorCodeEnumDeclaration);
            _fillErrorCodeEnumDeclaration(errorCodeEnumDeclaration, engineDefinition);
        } else {
            javaFile.getParentFile().mkdirs();
            javaFile.createNewFile();
            cu = new CompilationUnit(javaPackageName);
            EnumDeclaration errorCodeEnumDeclaration = cu.addEnum(error_code_enum_class_name, Modifier.PUBLIC);
            errorCodeEnumDeclaration.addImplementedType(ResponseCode.class);
            BlockStmt constructorBody = new BlockStmt();
            constructorBody.addStatement(new AssignExpr(new FieldAccessExpr(new ThisExpr(), "code"), new NameExpr("code"), Operator.ASSIGN));
            constructorBody.addStatement(new AssignExpr(new FieldAccessExpr(new ThisExpr(), "message"), new NameExpr("message"), Operator.ASSIGN));
            errorCodeEnumDeclaration.addConstructor(Modifier.PRIVATE).addParameter(PrimitiveType.intType(), "code").addParameter(String.class, "message").setBody(constructorBody);
            errorCodeEnumDeclaration.addField(PrimitiveType.intType(), "code", Modifier.PRIVATE, Modifier.FINAL).createGetter();
            errorCodeEnumDeclaration.addField(String.class, "message", Modifier.PRIVATE, Modifier.FINAL).createGetter();
            _fillErrorCodeEnumDeclaration(errorCodeEnumDeclaration, engineDefinition);
        }
        OutputStream output = new FileOutputStream(javaFile);
        IOUtils.write(cu.toString(), output, encoding);
        IOUtils.closeQuietly(output);
    }

    private static void _deleteErrorCodeEnumDeclaration(EnumDeclaration errorCodeEnumDeclaration) {
        for (Object enumConstantDeclaration : errorCodeEnumDeclaration.getEntries().toArray()) {
            errorCodeEnumDeclaration.remove((Node) enumConstantDeclaration);
        }
    }

    private static void _fillErrorCodeEnumDeclaration(EnumDeclaration errorCodeEnumDeclaration, EngineDefinition engineDefinition) {
        for (ErrorCodeDefinition errorCodeDefinition : engineDefinition.getErrorCodeDefinitions()) {
            errorCodeEnumDeclaration.addEnumConstant(errorCodeDefinition.getName()).setJavadocComment(errorCodeDefinition.getMessage()).addArgument(new IntegerLiteralExpr(errorCodeDefinition.getCode())).addArgument(new StringLiteralExpr(errorCodeDefinition.getMessage()));
        }
    }
}
