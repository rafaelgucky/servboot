package net.servboot.io;

import net.servboot.utils.io.NameGenerator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FormDataReader {
    private final InputStream in;
    private InputStreamReader reader;
    private final ShortArrayInputStream shortArray;
    File baseDir = new File(System.getProperty("java.io.tmpdir") + "/servboot/");
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

        totalReady += shortArray.reload((short) 10);

        try{
            String fileName = "";
            while (totalReady < length) {
                String line = readLine();
                if (!line.isEmpty() && !line.contains(boundary)) {
                    List<Integer> bytes;
                    if(line.contains("filename=\"")){
                        fileName = getKey(line);
                    } else if (line.contains("Content-Type")) {
                        String temp = line.split(":")[1].trim();
                        String contentType = temp.substring(temp.indexOf("/") + 1);
                        File file = readBytes(contentType);
                        if(formData.containsKey(fileName)){
                            ((List<File>) formData.get(fileName)).add(file);
                        } else{
                            formData.put(fileName, new LinkedList<>(List.of(file)));
                        }
                    } else if (line.contains("\"")) {
                        String key = getKey(line);
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

                totalReady += readed > 127 ? 2 : 1;
                line += tempBuffer[0];
            } while(tempBuffer[0] != 10 && (totalReady + tempBuffer.length) <= length);
        } catch (IOException ex) {
            System.out.println("Erro ao ler linha: " + ex.getMessage());
            ex.printStackTrace();
        }

        return line.replace("\r", "").replace("\n", "");
    }

    private File readBytes(String extension){
        File file;
        int[] tempBuffer = new int[this.boundary.length()];

        do{
            file = new File(Path.of(baseDir.toPath().toString(), "/", NameGenerator.generateName(extension)).toString());
        } while (file.exists());

        try{
            if(!baseDir.exists()){
                Files.createDirectory(Path.of(baseDir.toPath().toString()));
            }
            if(!file.createNewFile()) throw new IOException("Erro ao criar o arquivo: " + file.getName());
        } catch (IOException ex) {
            System.out.println("Erro ao criar arquivo: " + ex.getMessage());
            ex.printStackTrace();
        }

        try(FileOutputStream fos = new FileOutputStream(file)){
            // Consume linhas vazia para chegar aos bytes do arquivo
            int tempReaded = 0;
            while((tempReaded += shortArray.reload((short) 10)) >= 50);
            totalReady += tempReaded;

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
                        fos.write(tempBuffer[i]);
                    }
                } else {
                    break;
                }
            } while (totalReady < length);
        } catch (IOException ex){
            System.out.println("Erro ao ler bytes do arquivo: " + ex.getMessage());
            ex.printStackTrace();
        }

        return file;
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

    private String getKey(String line){
        return line.substring(line.indexOf("\"") + 1, line.indexOf("\"", line.indexOf("\"") + 1));
    }

}
