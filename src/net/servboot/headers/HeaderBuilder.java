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
        StringBuilder header = new StringBuilder();

        header.append("HTTP/1.1 ").append(responseCode).append(" ").append(FormatStringUtils.addSpaceOnUpperCase(StatusCode.getFromCode(responseCode).name())).append("\r\n");
        switch (headers.getValue()) {
            case 0:
                header.append("Content-Type: text/html; charset=UTF-8\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 1:
                header.append("Content-Type: application/json; charset=UTF-8\r\n");
                break;
            case 2:
                header.append("Content-Type: image/x-icon\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 3:
                header.append("Content-Type: image/png\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 4:
                header.append("Content-Type: image/jpg\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 5:
                header.append("Content-Type: image/jpeg\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 6:
            case 7:
                header.append("Content-Type: text/plain; charset=UTF-8\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
            case 8:
                header.append("Content-Type: application/pdf; charset=UTF-8\r\n");
                header.append("Content-Disposition: ").append(download ? "attachment" : "inline").append("; filename=").append("\"").append(fileName).append("\"").append("\r\n");
                break;
        }

        header.append("Content-Length: ").append(contentLength).append("\r\n");
        header.append("Cache-Control: no-cache, no-store, must-revalidate\r\n");
        header.append("Pragma: no-cache\r\n");
        header.append("Expires: 0\r\n");
        header.append("Access-Control-Allow-Origin: *\r\n");
        header.append("Connection: close\r\n");
        header.append("\r\n");

        return header.toString().getBytes(StandardCharsets.UTF_8);
    }
}
