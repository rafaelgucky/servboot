package net.servboot.io;

import java.io.IOException;
import java.io.InputStream;

public class ShortArrayInputStream extends InputStream {
    private final short MAX_BUFFER = 255;
    private final short[] buffer = new short[MAX_BUFFER];
    private final InputStream in;
    private int countBL = 0;
    private int pointer = 0;
    private short maxLength = 0;


    public ShortArrayInputStream(InputStream in){
        this.in = in;
        this.maxLength = fillBuffer();
    }

    @Override
    public int read() {
        if(this.pointer >= this.maxLength && (this.maxLength = fillBuffer()) <= 0) return -1;
        return this.buffer[this.pointer++];
    }

    public void reload(int length) {
        if (length > this.MAX_BUFFER) {
            length = this.MAX_BUFFER;
        }

        this.pointer = 0;
        this.maxLength = (short) length;
        short current = 0;

        try{
            for (int i = 0; i < length; i++) {
                buffer[current++] = (short) in.read();
            }
        } catch (IOException ex) {
            System.out.println("Erro na classe ShortArrayInputStream, método Reload(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public short reload(short targetChar){
        this.pointer = 0;
        short current = 0;

        try{
            do {
                if(current >= MAX_BUFFER) break;
                buffer[current++] = (short) in.read();
            } while (buffer[current - 1] != targetChar);
        } catch (IOException ex) {
            System.out.println("Erro na classe ShortArrayInputStream, método Reload(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }

        return this.maxLength = current;
    }

    private short fillBuffer() {
        short current = 0;
        this.pointer = 0;

        try{
            while(this.countBL < 4 && current < this.MAX_BUFFER){
                this.buffer[current++] = (short) this.in.read();
                if(this.buffer[current - 1] == 10 || this.buffer[current - 1] == 13){
                    this.countBL++;
                } else {
                    this.countBL = 0;
                }
            }
        } catch(IOException ex){
            System.out.println("Erro ao ler ShortArray: " + ex.getMessage());
            ex.printStackTrace();
        }

        return current;
    }
}