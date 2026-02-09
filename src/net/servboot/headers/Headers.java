package net.servboot.headers;

public enum Headers {
    TEXT_HTML(0),
    APPLICATION_JSON(1),
    IMAGE_ICON(2),
    IMAGE_PNG(3),
    IMAGE_JPG(4),
    IMAGE_JPEG(5),
    TEXT_TXT(6);

    private final int value;

    Headers(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Headers getValueFromFileExtension(String extension){
        return switch (extension){
                case "html" ->  Headers.TEXT_HTML;
                case "json" ->  Headers.APPLICATION_JSON;
                case "ico" ->  Headers.IMAGE_ICON;
                case "png" ->  Headers.IMAGE_PNG;
                case "jpg" ->  Headers.IMAGE_JPG;
                case "jpeg" ->  Headers.IMAGE_JPEG;
                default -> Headers.TEXT_TXT;
                };
    }
}
