package net.servboot.utils.strings;

public class FormatStringUtils {
    public static String addSpaceOnUpperCase(String toFormat) {
        String result = "";

        for(char c : toFormat.toCharArray()){
            result += Character.isUpperCase(c) ? " " + c : c;
        }

        return result;
    }
}
