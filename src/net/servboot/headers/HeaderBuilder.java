package net.servboot.headers;

import java.nio.charset.StandardCharsets;

public final class HeaderBuilder {
    public static byte[] build(Headers headers, long contentLength) {
        return build(headers, "",  contentLength);
    }

    public static byte[] build(Headers headers, long contentLength, String fileName){
        return build(headers, fileName, contentLength);
    }

    private static byte[] build(Headers headers, String fileName, long contentLength) {
        byte[] headersFileTypes = {2, 3, 4, 5};
        for (byte headersFileType : headersFileTypes) {
            if (headersFileType == headers.getValue() && (fileName == null || fileName.isEmpty())) {
                throw new RuntimeException("Headers file types don't match");
            }
        }

        StringBuilder header = new StringBuilder();
        switch (headers.getValue()) {
            case 0:
                header.append("HTTP/1.1 200 OK\r\n");
                header.append("Content-Type: text/html; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Connection: close\r\n");
                header.append("\r\n");
                break;
            case 1:
                header.append("HTTP/1.1 200 OK\r\n");
                header.append("Content-Type: image/xico\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: inline; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("\r\n");
                break;
            case 3:
                header.append("HTTP/1.1 200 OK\r\n");
                header.append("Content-Type: image/png\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: inline; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("\r\n");
                break;
            case 4:
                header.append("HTTP/1.1 200 OK\r\n");
                header.append("Content-Type: image/jpg\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: inline; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("\r\n");
                break;
        }
        return header.toString().getBytes(StandardCharsets.UTF_8);
    }
}
