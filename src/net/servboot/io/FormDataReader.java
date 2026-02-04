package net.servboot.io;

import net.servboot.utils.io.NameGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class FormDataReader {
    private final InputStream in;
    private InputStreamReader reader;
    private final ShortArrayInputStream shortArray;
    private final String boundary;
    private long length;
    private long totalReady = 0;

    public FormDataReader(InputStream in, InputStreamReader isr, ShortArrayInputStream shortArray, String boundary, long length) {
        this.in = in;
        this.reader = isr;
        this.boundary = boundary;
        this.length = length;
        this.shortArray = shortArray;
    }

    public Map<String, Object> readFormData() {
        Map<String, Object> formData = new LinkedHashMap<>();

        shortArray.reload((short) 10);

        try{
            while (totalReady < length) {
                String line = readLine();
                if (!line.contains(boundary)) {
                    List<Integer> bytes;
                    if (line.contains("Content-Type")) {
                        String temp = line.split(":")[1].trim();
                        String contentType = temp.substring(temp.indexOf("/") + 1);

                        bytes = readBytes();
                        File file = new File(NameGenerator.generateName(contentType));
                        if(!file.exists()){file.createNewFile();}
                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            for(int b : bytes){
                                fos.write(b);
                            }
                        } catch (IOException ex) {
                            System.out.println("Erro ao escrever no arquivo: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    } else if (!line.isEmpty() && !line.equals("--") && !line.contains("filename=\"")) {
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
                int readed = reader.read(tempBuffer);
                if(readed == -1) {
                    shortArray.reload((short) 10);
                    readed = reader.read(tempBuffer);
                }
                totalReady += readed;
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
        int[] tempBuffer = new int[this.boundary.length() + 2];

        try{
            // Consume linhas vazia para chegar aos bytes do arquivo
            while(!shortArray.reload((short) 10));

            do{
                int tempTotalRead = 0;
                int length = 0;
                int readed;
                do{
                    readed = in.read();
                    totalReady++;
                    tempTotalRead++;
                    tempBuffer[length++] = readed;
                }
                while(readed != 10 && length < tempBuffer.length);
                if(!bytesToString(tempBuffer).contains(boundary)){
                    for(int i = 0; i < tempTotalRead; i++){
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
