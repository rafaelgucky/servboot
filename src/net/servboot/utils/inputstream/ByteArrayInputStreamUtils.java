package net.servboot.utils.inputstream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ByteArrayInputStreamUtils {
    public static ByteArrayInputStream readRequestHeader(InputStream in){
        List<Byte> bytes = new LinkedList<>();
        ByteArrayInputStream byteArray = null;
        byte[] buffer;
        int countBL = 0;
        int readed;

        try{
            while(countBL < 4){
                readed = in.read();
                bytes.add((byte) readed);
                if(readed == 10 || readed == 13){
                    countBL++;
                } else {
                    countBL = 0;
                }
            }

            buffer = new byte[bytes.size()];

            for(int i = 0; i < bytes.size(); i++){
                buffer[i] = bytes.get(i);
            }

            byteArray = new ByteArrayInputStream(buffer);
        } catch(IOException ex){
            System.out.println("Erro ao ler ByteArray: " + ex.getMessage());
            ex.printStackTrace();
        }
        return byteArray;
    }
}
