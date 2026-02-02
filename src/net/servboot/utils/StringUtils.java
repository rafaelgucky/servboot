package net.servboot.utils;

public class StringUtils {
    public static String copy(String source){
        String result = "";
        for(char c : source.toCharArray()){
            result += c;
        }
        return result;
    }
}
