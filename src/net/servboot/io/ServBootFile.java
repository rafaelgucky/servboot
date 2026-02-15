package net.servboot.io;

import java.io.InputStream;

public class ServBootFile {
    private final InputStream inputStream;
    private final String fileName;
    private boolean download;
    private final String extension;

    public ServBootFile(InputStream inputStream, String fileName, boolean download) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.download = download;
        this.extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "txt";
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDownload() {
        return download;
    }
    public String getExtension() {
        return this.extension;
    }
}
