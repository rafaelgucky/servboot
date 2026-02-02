package net.servboot.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FormDataReader {
    private final InputStream in;
    private InputStreamReader reader;
    private final String boundary;
    private char[] buffer;
    private long length;
    private long totalReady = 0;

    public FormDataReader(InputStream in, InputStreamReader isr, String boundary, long length) {
        this.in = in;
        this.reader = isr;
        this.boundary = boundary;
        this.length = length;
    }

    public Map<String, Object> readFormData() {
        Map<String, Object> formData = new LinkedHashMap<>();
        buffer = new char[boundary.length() + 2];

        try{
            while (totalReady < length) {
                String line = readLine();
                if (!line.contains(boundary)) {
                    List<Integer> bytes = new LinkedList<>();
                    if (line.contains("Content-Type")) {
                        bytes = readBytes();
                        File file = new File("teste.txt");
                        if(!file.exists()){file.createNewFile();}
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            for(int b : bytes){
                                fos.write(b);
                            }
                        } catch (IOException ex) {
                            System.out.println("Erro ao escrever no arquivo: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                    if (!line.contains("filename=\"")) {
                        String key = line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
                        readLine();
                        String value = readLine();
                        formData.put(key, value);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro na leitura de arquivo binário: " + ex.getMessage());
            ex.printStackTrace();
        }
        return formData;
    }

    private String readLine(){
        String line = "";
        char[] tempBuffer = new char[1];

        try{
            do{
                totalReady += reader.read(tempBuffer);
                line += tempBuffer[0];
            } while(tempBuffer[0] != 10 && (totalReady + tempBuffer.length) <= length);
        } catch (IOException ex) {
            System.out.println("Erro ao ler linha: " + ex.getMessage());
            ex.printStackTrace();
        }

        return line.replace("\r", "").replace("\n", "");
    }

    private List<Integer> readBytes(){
        List<Integer> bytes = new LinkedList<>();
        int[] tempBuffer = new int[8096];
        //byte[] tempBuffer = new byte[boundary.length()];
        try{
            reader = null;
            do{
                int length = 0;
                int readed;
                do{
                    readed = in.read();
                    System.out.print(readed + " ");
                    totalReady += readed > 127 ? 2 : 1;
                    tempBuffer[length++] = readed;
                }
                while(readed != 10);
//                for(int i = 0; i < boundary.length(); i++){
//                    tempBuffer[i] = reader.read();
//                    totalReady += tempBuffer[i] > 127 ? 2 : 1;
//                }
                if(!bytesToString(tempBuffer).contains(boundary)){
                    for(int i = 0; i < boundary.length(); i++){
                        System.out.print(tempBuffer[i]);
                        bytes.add(tempBuffer[i]);
                    }
                } else {
                    System.out.println("ACHOU FIM");
                    break;
                }
            } while (totalReady < length);
        } catch (IOException ex){
            System.out.println("Erro ao ler bytes do arquivo: " + ex.getMessage());
            ex.printStackTrace();
        }
        return bytes;
    }

    private String bytesToString(int[] bytes){
        String result = "";
        for(int b : bytes){
            result += (char) b;
        }
        return result;
    }

    private String bytesToString(byte[] bytes){
        String result = "";
        for(byte b : bytes){
            result += (char) b;
        }
        return result;
    }

}
