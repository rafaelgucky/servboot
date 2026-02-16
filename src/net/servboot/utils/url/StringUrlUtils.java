package net.servboot.utils.url;

import net.servboot.request.Request;
import net.servboot.utils.StringUtils;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rafael Gucky
 * @since 01/2026
 * @version 1.0.0
 */
public class StringUrlUtils {
    public static String format(String url) {
        if(url == null || url.isEmpty()) return "";
        return url.substring(0, (url.lastIndexOf('/') >= url.length() - 1 ? url.lastIndexOf('/') : url.length()));
    }

    public static List<String> getPathParams(String url) {
        List<String> pathParams = new LinkedList<>();
        String tempParam = "";
        boolean controlAdd = false;

        for(char c : url.toCharArray()) {
            if(c == '}'){
                controlAdd = false;
                pathParams.add(tempParam);
                tempParam = "";
            } else if(controlAdd) {
                tempParam += c;
            }
            else if(c == '{'){
                controlAdd = true;
            }
        }

        return pathParams;
    }

    /**
     * @param request Request of client
     * @param apiUrl Api route
     * @return boolean
     * @throws MalformedURLException Throw this exception case the url of client is malformed
     */
    public static boolean match(Request request, String apiUrl) throws MalformedURLException {
        Map<String, String> params;

        if(apiUrl.chars().filter(c -> c == '/').count()
                != request.getUrl().chars().filter(c -> c == '/').count()) {
            return false;
        }
        else if(!apiUrl.contains("{")){
            return apiUrl.equalsIgnoreCase(request.getUrl());
        } else {
            params = extractUrlPathParams(request.getUrl(), apiUrl);
            request.addAllParameters(params);
            return !params.isEmpty() && apiUrl.chars()
                    .filter(c -> c == '{')
                    .count() == params.size();
        }
    }

    public static Map<String, String> extractUrlPathParams(String requestUrl, String apiUrl) throws MalformedURLException {
        Map<String, String> pathParams = new LinkedHashMap<>();
        String requestUrlCopy = StringUtils.copy(requestUrl);
        String apiUrlCopy = StringUtils.copy(apiUrl);
        String key = "";
        String value = "";

        format(requestUrlCopy);
        format(apiUrlCopy);

        while(!apiUrlCopy.isEmpty() || !requestUrlCopy.isEmpty()) {
            int index = apiUrlCopy.indexOf("{");
            if(index == -1  || index > requestUrlCopy.length()) break;

            apiUrlCopy = apiUrlCopy.substring(index + 1);
            requestUrlCopy = requestUrlCopy.substring(index);

            key = apiUrlCopy.substring(0, apiUrlCopy.indexOf("}"));
            value = requestUrlCopy.substring(0, requestUrlCopy.contains("/") ? requestUrlCopy.indexOf("/") : requestUrlCopy.length());

            requestUrlCopy = requestUrlCopy.substring(requestUrlCopy.contains("/") ? requestUrlCopy.indexOf("/") : requestUrlCopy.length() );
            apiUrlCopy = apiUrlCopy.substring(apiUrlCopy.contains("/") ? apiUrlCopy.indexOf("/") : apiUrlCopy.length());

            if(!key.isEmpty() && !value.isEmpty()){
                pathParams.put(key, value);
                key = "";
                value = "";
            }
        }

        if(!apiUrlCopy.isEmpty() || !requestUrlCopy.isEmpty()){
            throw new MalformedURLException("URL don't match! Comparing: '" + apiUrl + "' and '" + requestUrl + "'");
        }

        return pathParams;
    }

    public static String formatControllerUrl(String controllerUrl){
        String newUrl = "";

        for(int i = 0; i < controllerUrl.length(); i++){
            if(i == 0 && controllerUrl.charAt(i) != '/'){
                newUrl += "/";
            } else if(i == controllerUrl.length() - 1 && controllerUrl.charAt(i) != '/'){
                newUrl += controllerUrl.charAt(i);
                newUrl += "/";
                continue;
            }

            newUrl += controllerUrl.charAt(i);
        }

        return newUrl;
    }

    public static String formatMethodUrl(String methodUrl){
        String newUrl = "";

        for(int i = 0; i < methodUrl.length(); i++){
            if(i == 0 && methodUrl.charAt(i) == '/'){ continue; }
            newUrl += methodUrl.charAt(i);
        }

        return newUrl;
    }
}
