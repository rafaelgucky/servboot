package net.servboot.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShortArrayInputStream extends InputStream {
    List<Short> buffer = new ArrayList<>();
    private final InputStream in;
    int pointer;

    public ShortArrayInputStream(InputStream in){
        this.in = in;
        fillBuffer();
    }

    @Override
    public int read() throws IOException {
        if(pointer >= buffer.size()) return -1;
        return buffer.get(pointer++);
    }

    public void reload(int length){
        pointer = 0;

        try{
            buffer.clear();
            for(int i = 0; i < length; i++){
                buffer.add((short) in.read());
            }
            System.out.println("RECARREGADO");
        } catch (IOException ex) {
            System.out.println("Erro na classe ShortArrayInputStream, método Reload(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void reload(short byteChar){
        short readed;
        pointer = 0;

        try{
            buffer.clear();
            do {
                buffer.add((readed = (short) in.read()));
            } while (readed != byteChar);
        } catch (IOException ex) {
            System.out.println("Erro na classe ShortArrayInputStream, método Reload(). Message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void fillBuffer(){
        int countBL = 0;
        int readed;

        try{
            while(countBL < 4){
                readed = in.read();
                buffer.add((short) readed);
                if(readed == 10 || readed == 13){
                    countBL++;
                } else {
                    countBL = 0;
                }
            }
        } catch(IOException ex){
            System.out.println("Erro ao ler ByteArray: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
