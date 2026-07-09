package net.servboot.io;

import net.servboot.enums.EnumCharacter;
import net.servboot.utils.io.NameGenerator;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FormDataReader {
    private final InputStream in;
    private final InputStreamReader reader;
    private final ShortArrayInputStream shortArray;
    private final File baseDir = new File(System.getProperty("java.io.tmpdir") + "/servboot/");
    private final String boundary;
    private final long length;
    private long totalReady = 0;

    public FormDataReader(InputStream in, InputStreamReader isr, ShortArrayInputStream shortArray, String boundary, long length) {
        this.in = in;
        this.reader = isr;
        this.boundary = boundary;
        this.length = length;
        this.shortArray = shortArray;
    }

    /**
     * Read all form data received from client
     * @return key and values from form data
     */
    public Map<String, Object> readFormData() {
        Map<String, Object> formData = new LinkedHashMap<>();

        totalReady += shortArray.reload((short) 10);

        try{
            String fileName = "";
            while (this.totalReady < this.length) {
                String line = readLine();
                if (line.isEmpty() || line.contains(this.boundary)) continue;

                if(line.contains("filename=\"")){
                    fileName = getKey(line);
                } else if (line.contains("Content-Type")) {
                    String temp = line.split(":")[1].trim();
                    String contentType = temp.substring(temp.indexOf("/") + 1);
                    File file = this.getEmptyFile(contentType);
                    fillBytes(file);

                    // Adiciona o arquivo lido aos dados do formulário recebido
                    // Se a chave já existe, é porque é uma lista
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
        } catch (Exception ex) {
            System.out.println("Erro na leitura dos dados do formulário: " + ex.getMessage());
            ex.printStackTrace();
        }

        return formData;
    }

    /**
     * Read one line
     * @return one line
     */
    private String readLine(){
        String line = "";
        char[] tempBuffer = new char[1];

        try{
            do{
                int readed = reader.read(tempBuffer);
                if(readed == -1) {
                    shortArray.reload(EnumCharacter.LF.getCode());
                    readed = reader.read(tempBuffer);
                }

                totalReady += readed > 127 ? 2 : 1;
                line += tempBuffer[0];
            } while(tempBuffer[0] != EnumCharacter.LF.getCode() && (totalReady + tempBuffer.length) <= length);
        } catch (IOException ex) {
            System.out.println("Erro ao ler linha: " + ex.getMessage());
            ex.printStackTrace();
        }

        return line.replace("\r", "").replace("\n", "");
    }

    /**
     * Fill the file bytes
     * @param file target file
     */
    private void fillBytes(File file){
        int[] tempBuffer = new int[this.boundary.length()];

        try(FileOutputStream fos = new FileOutputStream(file)){
            // Consume linhas vazias para chegar aos bytes do arquivo
            int tempReaded = 0;
            while((tempReaded += shortArray.reload(EnumCharacter.LF.getCode())) >= 50);
            totalReady += tempReaded;

            do{
                int tempTotalRead = 0;
                int length = 0;
                int readed;
                do{
                    readed = in.read();
                    this.totalReady++;
                    tempTotalRead++;
                    tempBuffer[length++] = readed;
                }
                // Se não for fim de linha (para que leia o boundary corretamente) e ainda não encheu o buffer
                while(readed != EnumCharacter.LF.getCode() && length < tempBuffer.length);

                // Se não encontrou o boundary, adiciona aos bytes do arquivo, senão todo a arquivo já lido
                if(!bytesToString(tempBuffer).contains(boundary)){
                    for(int i = 0; i < tempTotalRead; i++){
                        fos.write(tempBuffer[i]);
                    }
                } else {
                    break;
                }
            } while (this.totalReady < this.length);
        } catch (IOException ex){
            System.out.println("Erro ao ler bytes do arquivo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Convert an integer array in a string
     * @param bytes array of bytes
     * @return bytes cast to string
     */
    private String bytesToString(int[] bytes){
        String result = "";
        for(int b : bytes){
            result += (char) b;
        }
        return result;
    }

    /**
     * Get key (name of input)
     * @param line line to extract the key
     * @return key of value
     */
    private String getKey(String line){
        int firstDubleQuotes =  line.indexOf('"');
        return line.substring(firstDubleQuotes + 1, line.indexOf("\"", firstDubleQuotes + 1));
    }

    /**
     * Get a empty file
     * @param extension extension of file
     * @return free empty file
     */
    private File getEmptyFile(String extension) throws IOException{
        File file;

        if(!this.baseDir.exists()) {
            Files.createDirectory(Path.of(this.baseDir.toPath().toString()));
        }

        do{
            file = new File(Path.of(this.baseDir.toPath().toString(), "/", NameGenerator.generateName(extension)).toString());
        } while (file.exists());

        if(!file.createNewFile()) throw new IOException("Erro ao criar o arquivo: " + file.getName());

        return file;
    }
}
