package com.farhanali.lite.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class CookieFormatter {
    
    public static final int FORMAT_STRING = 0;
    public static final int FORMAT_NETSCAPE = 1;
    public static final int FORMAT_JSON_ARRAY = 2;
    public static final int FORMAT_JSON_DICT = 3;

    /**
     * Convert standard cookie string to String format (passthrough)
     */
    public static String toStringFormat(String cookies) {
        if (cookies == null || cookies.trim().isEmpty()) {
            return "";
        }
        return cookies.trim();
    }

    /**
     * Convert standard cookie string to Netscape format
     */
    public static String toNetscapeFormat(String cookies, String domain) {
        if (cookies == null || cookies.trim().isEmpty()) {
            return "# Netscape HTTP Cookie File\n";
        }

        StringBuilder netscape = new StringBuilder("# Netscape HTTP Cookie File\n");
        String[] cookiePairs = cookies.split(";");
        
        long expires = System.currentTimeMillis() / 1000 + (365 * 24 * 60 * 60); // 1 year

        for (String pair : cookiePairs) {
            pair = pair.trim();
            if (pair.isEmpty()) continue;
            
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String name = parts[0].trim();
                String value = parts[1].trim();
                
                netscape.append(domain)
                        .append("\tTRUE\t/\tFALSE\t")
                        .append(expires)
                        .append("\t")
                        .append(name)
                        .append("\t")
                        .append(value)
                        .append("\n");
            }
        }
        
        return netscape.toString();
    }

    /**
     * Convert standard cookie string to JSON Array format with full details
     */
    public static String toJsonArrayFormat(String cookies, String domain) {
        if (cookies == null || cookies.trim().isEmpty()) {
            return "[]";
        }

        try {
            JSONArray jsonArray = new JSONArray();
            String[] cookiePairs = cookies.split(";");
            
            long expires = System.currentTimeMillis() / 1000 + (365 * 24 * 60 * 60); // 1 year

            for (String pair : cookiePairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;
                
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    JSONObject cookieObj = new JSONObject();
                    cookieObj.put("name", parts[0].trim());
                    cookieObj.put("value", parts[1].trim());
                    cookieObj.put("domain", domain);
                    cookieObj.put("path", "/");
                    cookieObj.put("expires", expires);
                    cookieObj.put("httpOnly", false);
                    cookieObj.put("secure", true);
                    cookieObj.put("sameSite", "None");
                    
                    jsonArray.put(cookieObj);
                }
            }
            
            return jsonArray.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Convert standard cookie string to JSON Dictionary format
     */
    public static String toJsonDictFormat(String cookies) {
        if (cookies == null || cookies.trim().isEmpty()) {
            return "{}";
        }

        try {
            JSONObject jsonDict = new JSONObject();
            String[] cookiePairs = cookies.split(";");

            for (String pair : cookiePairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;
                
                String[] parts = pair.split("=", 2);
                if (parts.length == 2) {
                    jsonDict.put(parts[0].trim(), parts[1].trim());
                }
            }
            
            return jsonDict.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    /**
     * Parse standard string format to cookie string (passthrough)
     */
    public static String fromStringFormat(String str) {
        if (str == null || str.trim().isEmpty()) {
            return "";
        }
        return str.trim();
    }

    /**
     * Parse Netscape format to standard cookie string
     */
    public static String fromNetscapeFormat(String netscape) {
        if (netscape == null || netscape.trim().isEmpty()) {
            return "";
        }

        StringBuilder cookies = new StringBuilder();
        String[] lines = netscape.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length >= 7) {
                String name = parts[5];
                String value = parts[6];
                
                if (cookies.length() > 0) {
                    cookies.append("; ");
                }
                cookies.append(name).append("=").append(value);
            }
        }

        return cookies.toString();
    }

    /**
     * Parse JSON Array format to standard cookie string
     */
    public static String fromJsonArrayFormat(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }

        try {
            JSONArray jsonArray = new JSONArray(json);
            StringBuilder cookies = new StringBuilder();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject cookie = jsonArray.getJSONObject(i);
                String name = cookie.optString("name", "");
                String value = cookie.optString("value", "");

                if (!name.isEmpty()) {
                    if (cookies.length() > 0) {
                        cookies.append("; ");
                    }
                    cookies.append(name).append("=").append(value);
                }
            }

            return cookies.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Parse JSON Dictionary format to standard cookie string
     */
    public static String fromJsonDictFormat(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }

        try {
            JSONObject jsonDict = new JSONObject(json);
            StringBuilder cookies = new StringBuilder();
            
            JSONArray names = jsonDict.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String name = names.getString(i);
                    String value = jsonDict.getString(name);

                    if (cookies.length() > 0) {
                        cookies.append("; ");
                    }
                    cookies.append(name).append("=").append(value);
                }
            }

            return cookies.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Auto-detect cookie format
     * @return FORMAT_STRING, FORMAT_NETSCAPE, FORMAT_JSON_ARRAY, or FORMAT_JSON_DICT
     */
    public static int detectFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return FORMAT_STRING;
        }

        String trimmed = input.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return FORMAT_JSON_ARRAY;
        }

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return FORMAT_JSON_DICT;
        }

        if (trimmed.contains("# Netscape") || trimmed.contains("\t")) {
            return FORMAT_NETSCAPE;
        }

        return FORMAT_STRING;
    }

    /**
     * Parse any format to standard cookie string
     */
    public static String parseToString(String input) {
        int format = detectFormat(input);
        
        switch (format) {
            case FORMAT_NETSCAPE:
                return fromNetscapeFormat(input);
            case FORMAT_JSON_ARRAY:
                return fromJsonArrayFormat(input);
            case FORMAT_JSON_DICT:
                return fromJsonDictFormat(input);
            case FORMAT_STRING:
            default:
                return fromStringFormat(input);
        }
    }

    /**
     * Convert from standard string to specified format
     */
    public static String convertToFormat(String cookies, int format, String domain) {
        switch (format) {
            case FORMAT_NETSCAPE:
                return toNetscapeFormat(cookies, domain);
            case FORMAT_JSON_ARRAY:
                return toJsonArrayFormat(cookies, domain);
            case FORMAT_JSON_DICT:
                return toJsonDictFormat(cookies);
            case FORMAT_STRING:
            default:
                return toStringFormat(cookies);
        }
    }
}
