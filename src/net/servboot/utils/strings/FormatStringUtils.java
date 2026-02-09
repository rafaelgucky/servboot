package net.servboot.utils.strings;

public class FormatStringUtils {
    public static String addSpaceOnUpperCase(String toFormat){
        String result = "";

        for(char c : toFormat.toCharArray()){
            if(Character.isUpperCase(c)){
                result += " ";
            }
            result += c;
        }

        return result;
    }
}
