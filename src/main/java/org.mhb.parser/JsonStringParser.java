package org.mhb.parser;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonStringParser {

    private static final Logger logger = Logger.getLogger(JsonStringParser.class.getSimpleName());
    public static final String JSON_TYPE_INTEGER = "integer";
    public static final String JSON_TYPE_NUMBER = "number";
    private static final List<String> MIN_PROPERTY_NAMES = Arrays.asList("minimum", "minLength");
    private static final List<String> MAX_PROPERTY_NAMES = Arrays.asList("maximum", "maxLength");
    public static final String TYPE = "type";
    public static final String MINIMUM = "minimum";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAXIMUM = "maximum";
    public static final String MAX_LENGTH = "maxLength";

    private Deque<RecordDetail> stack = new ArrayDeque<>();
    private List<RecordDetail> validProperties = new ArrayList<>();

    public List<RecordDetail> parseJsonString(String jsonString) {
        JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();

        for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if("definitions".equalsIgnoreCase(entry.getKey())) {
                if(entry.getValue().isJsonObject()) {
                    createElementObject(entry.getKey());
                    parseJsonElement(entry.getValue());
                    addValidProperties(stack.poll());
                }
                else {
                    addElementValue(entry);
                }
            }
        }

        int endIndex = jsonString.length() > 255 ? 254 : jsonString.length();
        logger.log(Level.FINER, "From json {0} found {1} TypeProperties", new Object[]{jsonString.substring(0, endIndex), validProperties.size()});
        return validProperties;
    }

    private void addValidProperties(RecordDetail properties) {
        if(Objects.nonNull(properties) && Objects.nonNull(properties.getType())
                && (Objects.nonNull(properties.getMinimumValue()) || Objects.nonNull(properties.getMaximumValue()))) {
            validProperties.add(properties);
        }
    }


    private void createElementObject(String key) {
        String currentKey = key;
        if(Objects.isNull(currentKey) || currentKey.indexOf("$") == 0) {
            return;
        }

        RecordDetail properties = new RecordDetail();
        properties.setName(currentKey);
        stack.push(properties);
    }


    public void parseJsonElement(JsonElement object) {

        for(Map.Entry<String, JsonElement> entry : object.getAsJsonObject().entrySet()) {
            if(entry.getValue().isJsonArray()) {
                parseJsonArray(entry);
            }
            else if(entry.getValue().isJsonObject()) {
                createElementObject(entry.getKey());
                parseJsonElement(entry.getValue());
                addValidProperties(stack.poll());
            }
            else {
                addElementValue(entry);
            }
        }
    }

    private void parseJsonArray(Map.Entry<String, JsonElement> entry) {
        JsonArray jsonArray = entry.getValue().getAsJsonArray();
        Iterator<JsonElement> iterator = jsonArray.iterator();
        while(iterator.hasNext()) {
            JsonElement next = iterator.next();
            parseJsonElement(next);
        }
    }

    private void addElementValue(Map.Entry<String, JsonElement> entry) {
        if(stack.size() == 0) {
            return; // ignored root elements
        }

        if(entry.getValue().isJsonObject()) {
            parseJsonElement(entry.getValue());
        }
        else {
            RecordDetail properties = stack.peek();
            setDataTypeProperties(entry, properties);
        }
    }

    private void setDataTypeProperties(Map.Entry<String, JsonElement> entry, RecordDetail recordDetail) {

        switch(entry.getKey()) {
            case TYPE:
                recordDetail.setType(entry.getValue().getAsString());
                break;
            case MINIMUM:
            case MIN_LENGTH:
                recordDetail.setMinimumValue(parseToGetMinimumValue(entry));
                break;
            case MAXIMUM:
            case MAX_LENGTH:
                recordDetail.setMaximumValue(parseToGetMaximumValue(entry, recordDetail));
                break;
            default:
                break;

        }
    }


    private Long parseToGetMinimumValue(Map.Entry<String, JsonElement> entry) {
        String value = entry.getValue().getAsString();
        value = stripDecimalPart(value);

        return Long.valueOf(value);
    }

    private String stripDecimalPart(String value) {
        int indexOfDot = value.indexOf(".");
        if(indexOfDot > 0) {
            value = value.substring(0, indexOfDot);
        }
        return value;
    }

    private Long parseToGetMaximumValue(Map.Entry<String, JsonElement> entry, RecordDetail type) {
        String value = entry.getValue().getAsString();
        value = stripDecimalPart(value);
        return Long.valueOf(value);
    }

}
