package net.servboot.utils.strings;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringUtils {

    public static boolean equalsIgnorePathParams(String endPoint, String url) {
        // Remove query params
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        endPoint = removeSuffix(removePrefix(endPoint.toLowerCase(), "/"), "/");
        url = removeSuffix(removePrefix(url.toLowerCase(), "/"), "/");

        if (!endPoint.contains("{")) return endPoint.equalsIgnoreCase(url);

        while (!endPoint.isEmpty() && !url.isEmpty()) {
            int paramIndex = endPoint.indexOf("{");
            endPoint = endPoint.substring(paramIndex);
            endPoint = endPoint.substring(endPoint.contains("/") ? endPoint.indexOf("/") : endPoint.length());
            url = url.substring(Math.min(paramIndex, url.length()));
            url = url.substring(url.contains("/") ? url.indexOf("/") : url.length());
        }

        return endPoint.length() == url.length();
    }

    public static Map<String, String> getPathParameters(String endPoint, String url) {
        Map<String, String> pathParameters = new LinkedHashMap<>();

        if (!endPoint.contains("{")) return pathParameters;

        // Remove query params
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }

        endPoint = removeSuffix(removePrefix(endPoint.toLowerCase(), "/"), "/");
        url = removeSuffix(removePrefix(url.toLowerCase(), "/"), "/");

        while (!endPoint.isEmpty() && !url.isEmpty()) {
            int paramIndex = endPoint.indexOf("{");
            endPoint = endPoint.substring(paramIndex);
            String paramName = endPoint.substring(0, endPoint.contains("/") ? endPoint.indexOf("/") : endPoint.length());
            endPoint = endPoint.substring(endPoint.contains("/") ? endPoint.indexOf("/") : endPoint.length());
            url = url.substring(Math.min(paramIndex, url.length()));
            String paramValue = url.substring(0, url.contains("/") ? url.indexOf("/") : url.length());
            url = url.substring(url.contains("/") ? url.indexOf("/") : url.length());

            pathParameters.put(paramName.replace("{", "").replace("}", ""), paramValue);
        }

        return pathParameters;
    }

    public static Map<String, String> getQueryParameters(String url) {
        Map<String, String> queryParameters = new LinkedHashMap<>();

        if (!url.contains("?")) return queryParameters;

        url = url.substring(url.indexOf("?") + 1);

        while (!url.isEmpty()) {
            String paramName = url.substring(0, url.indexOf("="));
            url = url.substring(url.indexOf("=") + 1);
            String paramValue = url.substring(0, url.contains("&") ? url.indexOf("&") : url.length());
            url = url.substring(url.contains("&") ? url.indexOf("&") + 1 : url.length());

            queryParameters.put(paramName, paramValue);
        }

        return queryParameters;
    }

    public static String reverse(String string) {
        String temp = "";
        for (int i = string.length() - 1; i >= 0; i--) {
            temp += string.charAt(i);
        }

        return temp;
    }

    public static String removePrefix(String target, String prefix) {
        return (target.startsWith(prefix)) ? target.substring(prefix.length()) : target;
    }

    public static String removeSuffix(String target, String suffix) {
        return (target.endsWith(suffix)) ? target.substring(0, target.length() - suffix.length()) : target;
    }
}
