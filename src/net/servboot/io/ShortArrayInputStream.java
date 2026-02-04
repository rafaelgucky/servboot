package net.servboot.io;

import java.io.IOException;
import java.io.InputStream;

public class ShortArrayInputStream extends InputStream {
    private final byte MAX_BUFFER = 60;
    private final short[] buffer = new short[MAX_BUFFER];
    private final InputStream in;
    private int countBL = 0;
    private int pointer = 0;
    private byte maxLength = 0;


    public ShortArrayInputStream(InputStream in){
        this.in = in;
        maxLength = fillBuffer();
    }

    @Override
    public int read() {
        if(pointer >= maxLength && (maxLength = fillBuffer()) <= 0) return -1;
        return buffer[pointer++];
    }

    public boolean reload(short targetChar){
        byte current = 0;

        pointer = 0;

        try{
            do {
                if(current >= MAX_BUFFER){
                    maxLength = current;
                    return false;
                }
                buffer[current++] = (short) in.read();
            } while (buffer[current - 1] != targetChar);
            maxLength = current;
        } catch (IOException ex) {
            System.out.println("Erro na classe ShortArrayInputStream, método Reload(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }

        return true;
    }

    private byte fillBuffer(){
        byte current = 0;

        try{
            pointer = 0;

            while(countBL < 4 && current < MAX_BUFFER){
                buffer[current++] = (short) in.read();
                if(buffer[current - 1] == 10 || buffer[current - 1] == 13){
                    countBL++;
                } else {
                    countBL = 0;
                }
            }
        } catch(IOException ex){
            System.out.println("Erro ao ler ByteArray: " + ex.getMessage());
            ex.printStackTrace();
        }

        return current;
    }
}
