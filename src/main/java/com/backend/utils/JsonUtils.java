package com.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class JsonUtils {
    private static final ObjectMapper om = new ObjectMapper();

    /**
     * Converts a Java object to a JSONObject.
     *
     * @param obj the Java object to convert
     * @return the converted JSONObject
     */
    public static JSONObject toJsonObj(Object obj) {
        try {
            String jsonString = om.writeValueAsString(obj);
            return new JSONObject(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts a Java object to a JSONArray.
     *
     * @param obj the Java object to convert
     * @return the converted JSONArray
     */
    public static JSONArray toJsonArray(Object obj) {
        try {
            String jsonString = om.writeValueAsString(obj);
            return new JSONArray(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts a JSONObject to a Java object of the specified type.
     *
     * @param jo the JSONObject to convert
     * @param clazz the class of the Java object
     * @return the converted Java object
     */
    public static <T> T toPojoObj(JSONObject jo, Class<T> clazz) {
        try {
            return om.readValue(jo.toString(), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Converts a JSONArray to a Java object of the specified type.
     *
     * @param ja the JSONArray to convert
     * @param clazz the class of the Java object
     * @return the converted Java object
     */
    public static <T> List<T> toPojoList(JSONArray ja, Class<T[]> clazz) {
        try {
            return List.of(om.readValue(ja.toString(), clazz));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to list of objects", e);
        }
    }
}
