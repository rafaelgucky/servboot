package net.servboot.utils.inputstream;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InputStreamUtils {
    public static String readLine(InputStream in) {
        int bt;
        StringBuilder line = new StringBuilder();
        try{
            while((bt = in.read()) != 10 && bt != -1){
                line.append((char) bt);
            }
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return line.toString();
    }

    public static List<Integer> readBytes(InputStream in, long length) {
        List<Integer> bytes = new LinkedList<>();
        try{
            for(int i = 0; i < length; i++){
                int x;
                bytes.add((x = in.read()));
                System.out.println(x);
            }
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return bytes;
    }
}
