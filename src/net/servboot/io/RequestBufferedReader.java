package net.servboot.io;

import java.io.IOException;
import java.io.InputStreamReader;

public class RequestBufferedReader {
    private final InputStreamReader isr;
    private final ShortArrayInputStream shortArray;
    private int totalReady = 0;

    public RequestBufferedReader(ShortArrayInputStream shortArray, InputStreamReader isr){
        this.shortArray = shortArray;
        this.isr = isr;
    }

    public String readBody(long bytes) throws Exception {
        char[] character = new char[1];
        String body = "";
        long readed;

        isr.read(character);

        while(totalReady <= bytes) {
            if((readed = isr.read(character)) == -1){
                shortArray.reload((short) 125);
                readed = isr.read(character);
            }

            totalReady += readed > 127 ? 2 : 1;
            body += character[0];
        }

        return body;
    }

    public String readLine(){
        char[] character = new char[1];
        String line = "";

        try {
            while(isr.read(character) != -1){
                if(character[0] != 13){
                    if(character[0] == 10) break;
                    else{
                        line += character[0];
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Erro ao ler header line: " + ex.getMessage());
            ex.printStackTrace();
        }

        return line;
    }
}