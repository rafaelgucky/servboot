package net.servboot.io;


import java.io.InputStreamReader;

public class RequestBufferedReader {
    public final byte MAX_BREAK_LINE = 4;
    private InputStreamReader isr;
    private final char[] buffer = new char[200];
    private boolean endHeader = false;
    private int count = 0;
    private int pointer = 0;
    private int maxIndex;
    private long extraBytes = 0;

    public RequestBufferedReader(InputStreamReader isr){
        this.isr = isr;
        maxIndex = loadBuffer(Long.MAX_VALUE);
    }

    public String readHeaderLine(){
        String line = "";

        while(pointer <= maxIndex){
            if(pointer == maxIndex){
                if((maxIndex = loadBuffer(Long.MAX_VALUE)) == 0) break;
                pointer = 0;
            } else {
                if(buffer[pointer] == 10){
                    pointer++;
                    break;
                }
                line += buffer[pointer++];
            }
        }
        return line;
    }

    public String readBody(long bytes) throws Exception{
        String body = "";
        long ready = 0;
        long length;


        if(!endHeader) throw new Exception("Necessário ler o header do request primeiro!");

        this.endHeader = false;
        this.pointer = 0;
        this.extraBytes = 0;

        do{
            ready += (length = loadBuffer(bytes - ready));
            for(int i = 0; i < length; i++){
                body = body + buffer[i];
            }
        } while(length == this.buffer.length);

        return body;
    }

    public void close(){
        this.isr = null;
    }

   private int loadBuffer(long maxLength){
        char[] tempBuffer;
        int length = 0;

        if(endHeader || maxLength <= 0) return length;

        tempBuffer = new char[1];

        try{
            do{
                if((length += isr.read(tempBuffer)) <= 0) return 0;
                if((short) tempBuffer[0] > 127 ) { extraBytes++; }
                System.arraycopy(tempBuffer, 0, buffer,  length - tempBuffer.length, tempBuffer.length);
                count = tempBuffer[0] == 13 || tempBuffer[0] == 10 ? count + 1 : 0;

                if(count == MAX_BREAK_LINE && maxLength == Long.MAX_VALUE){
                    endHeader = true;
                    return length;
                }
            } while((length + tempBuffer.length) <= buffer.length && (length + extraBytes + tempBuffer.length) <= maxLength);
        } catch(Exception ex){
          System.out.println("Erro ao ler buffer: " + ex.getMessage());
          ex.printStackTrace();
        }
        return length;
   }
}