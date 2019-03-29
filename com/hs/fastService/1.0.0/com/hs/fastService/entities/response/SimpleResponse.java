package com.hs.fastService.entities.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;

public class SimpleResponse extends BaseResponse {

    private HashMap<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    @JsonAnyGetter
    public HashMap<String, Object> getMap() {
        return map;
    }
}
