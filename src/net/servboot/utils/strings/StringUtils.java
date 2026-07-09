package net.servboot.utils.strings;

public class StringUtils {
    public static String reverse(String string) {
        String temp = "";
        for (int i = string.length() - 1; i >= 0; i--) {
            temp += string.charAt(i);
        }

        return temp;
    }
}
