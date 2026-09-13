package com.farhanali.lite.web;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CookieFormatter {
    private CookieFormatter() {}

    public static String convertToFormat(String cookies, int format, String domain) {
        if (cookies == null) return "";
        try {
            return switch (format) {
                case 1 -> toNetscape(cookies, domain);
                case 2 -> toJsonArray(cookies, domain);
                case 3 -> toJsonDict(cookies);
                default -> cookies.trim();
            };
        } catch (Exception e) {
            return cookies;
        }
    }

    public static String parse(String input, int format) {
        if (input == null || (input = input.trim()).isEmpty()) return "";
        try {
            return switch (format) {
                case 1 -> fromNetscape(input);
                case 2 -> fromJsonArray(input);
                case 3 -> fromJsonDict(input);
                default -> input;
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static Map<String, String> parsePairs(String cookies) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String pair : cookies.split(";")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                if (!key.isEmpty()) map.put(key, parts[1].trim());
            }
        }
        return map;
    }

    private static void appendCookie(StringBuilder sb, String name, String value) {
        if (name != null && !name.isEmpty()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(name).append('=').append(value != null ? value : "");
        }
    }

    private static String toNetscape(String cookies, String domain) {
        StringBuilder sb = new StringBuilder("# Netscape HTTP Cookie File\n");
        long expires = System.currentTimeMillis() / 1000 + (365L * 24 * 60 * 60);
        for (Map.Entry<String, String> entry : parsePairs(cookies).entrySet()) {
            sb.append(domain).append("\tTRUE\t/\tFALSE\t").append(expires)
              .append("\t").append(entry.getKey()).append("\t").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private static String toJsonArray(String cookies, String domain) throws Exception {
        JSONArray array = new JSONArray();
        long expires = System.currentTimeMillis() / 1000 + (365L * 24 * 60 * 60);
        for (Map.Entry<String, String> entry : parsePairs(cookies).entrySet()) {
            JSONObject obj = new JSONObject();
            obj.put("name", entry.getKey());
            obj.put("value", entry.getValue());
            obj.put("domain", domain);
            obj.put("path", "/");
            obj.put("expires", expires);
            array.put(obj);
        }
        return array.toString(4);
    }

    private static String toJsonDict(String cookies) throws Exception {
        JSONObject dict = new JSONObject();
        for (Map.Entry<String, String> entry : parsePairs(cookies).entrySet()) {
            dict.put(entry.getKey(), entry.getValue());
        }
        return dict.toString(4);
    }

    private static String fromNetscape(String netscape) {
        StringBuilder sb = new StringBuilder();
        for (String line : netscape.split("\n")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("\\s+");
            if (parts.length >= 7) appendCookie(sb, parts[5], parts[6]);
        }
        return sb.toString();
    }

    private static String fromJsonArray(String json) throws Exception {
        JSONArray array = new JSONArray(json);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            appendCookie(sb, obj.optString("name", ""), obj.optString("value", ""));
        }
        return sb.toString();
    }

    private static String fromJsonDict(String json) throws Exception {
        JSONObject dict = new JSONObject(json);
        StringBuilder sb = new StringBuilder();
        JSONArray names = dict.names();
        if (names != null) {
            for (int i = 0; i < names.length(); i++) {
                String name = names.getString(i);
                appendCookie(sb, name, dict.optString(name, ""));
            }
        }
        return sb.toString();
    }
}
