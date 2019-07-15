package org.mhb.parser;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParseJsonString {

    private static final Logger logger = Logger.getLogger(ParseJsonString.class.getSimpleName());
    public static final String JSON_TYPE_INTEGER = "integer";
    public static final String JSON_TYPE_NUMBER = "number";

    private Deque<RecordDetail> stack = new ArrayDeque<>();
    private List<RecordDetail> validProperties = new ArrayList<>();

    public List<RecordDetail> parseJsonString(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if ("definitions".equalsIgnoreCase(entry.getKey())) {
                if (entry.getValue().isJsonObject()) {
                    createElementObject(entry.getKey());
                    parseJsonElement(entry.getValue());
                    addValidProperties(stack.poll());
                } else {
                    addElementValue(entry);
                }
            }
        }

        int endIndex = jsonString.length() > 255 ? 254 : jsonString.length();
        logger.log(Level.FINER, "From json {0} found {1} TypeProperties", new Object[]{jsonString.substring(0, endIndex), validProperties.size()});
        return validProperties;
    }

    private void addValidProperties(RecordDetail properties) {
        if (Objects.nonNull(properties) && Objects.nonNull(properties.getType())
                && (Objects.nonNull(properties.getMinimumValue()) || Objects.nonNull(properties.getMaximumValue()))) {
            validProperties.add(properties);
        }
    }


    private void createElementObject(String key) {
        String currentKey = key;
        if (Objects.isNull(currentKey) || currentKey.indexOf("$") == 0) {
            return;
        }

        RecordDetail properties = new RecordDetail();
        properties.setName(currentKey);
        stack.push(properties);
    }


    public void parseJsonElement(JsonElement object) {

        for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject().entrySet()) {
            //Hack: igonre HIGH_LIGHT object
            if ("HIGH_LIGHT".equalsIgnoreCase(entry.getKey())) {
                continue;
            }

            if (entry.getValue().isJsonArray()) {
                parseJsonArray(entry);
            } else if (entry.getValue().isJsonObject()) {
                createElementObject(entry.getKey());
                parseJsonElement(entry.getValue());
                addValidProperties(stack.poll());
            } else {
                addElementValue(entry);
            }
        }
    }

    private void parseJsonArray(Map.Entry<String, JsonElement> entry) {
        JsonArray jsonArray = entry.getValue().getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {
            JsonElement next = iterator.next();
            parseJsonElement(next);
        }
    }

    private void addElementValue(Map.Entry<String, JsonElement> entry) {
        if (stack.size() == 0) {
            return; // ignored root elements
        }

        if (entry.getValue().isJsonObject()) {
            parseJsonElement(entry.getValue());
        } else {
            RecordDetail properties = stack.peek();
            setDataTypeProperties(entry, properties);
        }
    }

    private void setDataTypeProperties(Map.Entry<String, JsonElement> entry, RecordDetail properties) {

        switch (entry.getKey()) {
            case "type":
                properties.setType(entry.getValue().getAsString());
                break;
            case "minimum":
            case "minLength":
                properties.setMinimumValue(parseToGetMinimumValue(entry));
                break;
            case "maximum":
            case "maxLength":
                properties.setMaximumValue(parseToGetMaximumValue(entry, properties));
                break;
            default:
                break;

        }
    }


    private Long parseToGetMinimumValue(Map.Entry<String, JsonElement> entry) {
        String value = entry.getValue().getAsString();
        return Objects.isNull(value) || value.startsWith("-") ? null : Long.valueOf(value);
    }

    private Long parseToGetMaximumValue(Map.Entry<String, JsonElement> entry, RecordDetail type) {
        Long maxValue;

        if (JSON_TYPE_INTEGER.equalsIgnoreCase(type.getType()) || JSON_TYPE_NUMBER.equalsIgnoreCase(type.getType())) {
            String value = entry.getValue().getAsString();
            maxValue = Objects.isNull(value) ? null : Long.valueOf(value.length());
        } else {
            maxValue = entry.getValue().getAsLong();
        }

        if (Objects.equals("GPGES_BETEIL_BETR", type.getName())) {
            String s = entry.getValue().getAsString();
            Long l = entry.getValue().getAsLong();
            BigDecimal b = entry.getValue().getAsBigDecimal();
        }

        return maxValue;
    }
}
