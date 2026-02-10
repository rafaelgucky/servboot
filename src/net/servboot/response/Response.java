package net.servboot.response;

public class Response {
    private short responseCode;
    private Object body;

    public Response(int responseCode){
        this.responseCode = (short) responseCode;
    }

    public short getResponseCode(){
        return responseCode;
    }

    public Response setBody(Object object){
        this.body = object;
        return this;
    }

    public Object getBody(){
        return this.body;
    }
}
