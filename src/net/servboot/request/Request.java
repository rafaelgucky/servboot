package net.servboot.request;

import net.servboot.utils.url.StringUrlUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private final Map<String, String> cookies = new LinkedHashMap<>();
    private final Map<String, List<File>> files = new LinkedHashMap<>();
    private Class<?> clazz;
    private Method method;
    private String stringMethod;
    private String url;
    private String stringBody = "";
    private String contentType = "";
    private int contentLength;

    public Request(String method, String url) {
        this.stringMethod = method.trim();
        this.url = StringUrlUtils.format(url.trim());
    }

    public void addParameter(String key, String value){
        parameters.put(key, value);
    }

    public void addAllParameters(Map<String, String> parameters){
        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            addParameter(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void addHeader(String header){
        String[] pairs = extractHeaderKeyValue(header);
        if(pairs.length != 2) return;
        if(pairs[0].contains("Content-Length")){
            contentLength = Integer.parseInt(pairs[1]);
        } else if(pairs[0].contains("Content-Type")){
            this.contentType = pairs[1].substring(0, pairs[1].contains(";") ? pairs[1].indexOf(";") : pairs[1].length()).trim();
            if(pairs[1].contains("multipart/form-data")){
                String boundary = pairs[1].substring(pairs[1].indexOf("=") + 1);
                pairs[0] = "boundary";
                pairs[1] = boundary;
            }
        } else if(pairs[0].contains("Cookie")){
            if(pairs[1].contains(";")){
                pairs = pairs[1].split(";");
            } else if(!pairs[1].contains("=")) return;
            String[] kv = pairs[1].split("=");
            for(int i = 0; i < kv.length; i += 2){
                cookies.put(kv[i].trim(), kv[i + 1].trim());
            }
        }
        headers.put(pairs[0], pairs[1]);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key){
        String value = "";

        if(headers.containsKey(key)){
            value = headers.get(key);
        }

        return value;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String getCookie(String key){
        String value = "";

        if(cookies.containsKey(key)){
            value = cookies.get(key);
        }

        return value;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void setStringMethod(String method) {
        this.stringMethod = method;
    }

    public String getStringMethod() {
        return stringMethod;
    }

    public void setUrl(String url) {
        this.url = StringUrlUtils.format(url.trim());
    }

    public String getUrl() {
        return url;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setStringBody(String stringBody) {
        this.stringBody = stringBody;
    }

    public String getStringBody() {
        return stringBody;
    }

    public void addLineToStringBody(String line){
        stringBody += line;
    }

    public String getContentType() {
        return contentType;
    }

    public void addFile(String name, List<File> file){
        files.put(name, file);
    }

    public Map<String, List<File>> getFiles() {
        return files;
    }

    public List<File> getFile(String name){
        return files.get(name);
    }

    public void setContentLength(String contentLength) {
        if(!contentLength.contains("Content-Length")) throw new  IllegalArgumentException("Content-Length is invalid");

        contentLength = contentLength.trim().substring(contentLength.indexOf(":") + 1).trim();
        try{
            this.contentLength = Integer.parseInt(contentLength);
        } catch(NumberFormatException ex){
            System.out.println("Erro ao carregar conteúdo: " + ex.getMessage());
        }
    }

    private String[] extractHeaderKeyValue(String headerLine){
        String[] pairs = headerLine.trim().split(":", 2);

        if(pairs.length == 2){
            for(int i = 0; i < pairs.length; i++){
                pairs[i] = pairs[i].trim();
            }
        }

        return pairs;
    }
}
