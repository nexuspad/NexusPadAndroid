/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class JsonHelper {

    public static int getIntValue(JSONObject jsonObj, String name) {
        try {
            String value = jsonObj.getString(name);
            if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("false")) {
                return 0;
            }

            return Integer.parseInt(value);

        } catch (JSONException e) {
            return -1;
        }
    }

    public static Object toJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), toJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            @SuppressWarnings("rawtypes")
            Iterable it = (Iterable) object;
            for (Object value : it) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws JSONException {
        return toMap(object.getJSONObject(key));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> nameItr = object.keys();

        while (nameItr.hasNext()) {
            String name = nameItr.next();
            map.put(name, object.getString(name));

        }

        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
}
