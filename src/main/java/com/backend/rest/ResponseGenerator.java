package com.backend.rest;

import java.util.HashMap;
import java.util.Map;

public class ResponseGenerator {

    public static Map<String, Object> createFromExc(Exception exc) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", exc.getMessage());

        return responseBody;
    }

}
