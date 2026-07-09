package net.servboot.enums;

public enum EnumCharacter {
    LF((short) 10),
    CR((short) 13);

    private final short code;

    EnumCharacter(short code) {
        this.code = code;
    }

    public short getCode() {
        return this.code;
    }
}
