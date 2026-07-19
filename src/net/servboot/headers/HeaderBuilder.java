package net.servboot.headers;

import net.servboot.response.StatusCode;
import net.servboot.utils.strings.FormatStringUtils;

import java.nio.charset.StandardCharsets;

public final class HeaderBuilder {
    public static byte[] build(Headers headers, short responseCode, long contentLength) {
        return build(headers, responseCode,"",  contentLength, false);
    }

    public static byte[] build(Headers headers, short responseCode, long contentLength, boolean download, String fileName){
        return build(headers, responseCode, fileName, contentLength, download);
    }

    private static byte[] build(Headers headers, short responseCode, String fileName, long contentLength, boolean download) {
//        byte[] headersFileTypes = {2, 3, 4, 5};
//        for (byte headersFileType : headersFileTypes) {
//            if (headersFileType == headers.getValue() && (fileName == null || fileName.isEmpty())) {
//                throw new RuntimeException("Headers file types don't match");
//            }
//        }

        StringBuilder header = new StringBuilder();
        switch (headers.getValue()) {
            case 0:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: text/html; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                break;
            case 1:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: application/json; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 2:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: image/x-icon\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 3:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: image/png\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 4:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: image/jpg\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 5:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: image/jpeg\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 6:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: text/plain; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 7:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: text/plain; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
            case 8:
                header.append("HTTP/1.1 ");
                header.append(responseCode);
                header.append(" ");
                header.append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name()));
                header.append("\r\n");
                header.append("Content-Type: application/pdf; charset=UTF-8\r\n");
                header.append("Content-Length: ");
                header.append(contentLength);
                header.append("\r\n");
                header.append("Content-Disposition: ");
                header.append(download ? "attachment" : "inline");
                header.append("; filename=");
                header.append("\"");
                header.append(fileName);
                header.append("\"\r\n");
                header.append("Connection: close\r\n");
                header.append("Access-Control-Allow-Origin: *\r\n");
                header.append("\r\n");
                break;
        }
        return header.toString().getBytes(StandardCharsets.UTF_8);
    }
}
