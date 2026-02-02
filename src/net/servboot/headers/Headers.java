package net.servboot.headers;

public enum Headers {
    TEXT_HTML(0),
    APPLICATION_JSON(1),
    IMAGE_ICON(2),
    IMAGE_PNG(3),
    IMAGE_JPG(4),
    IMAGE_JPEG(5);

    private final int value;

    Headers(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
