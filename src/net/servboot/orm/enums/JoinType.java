package net.servboot.orm.enums;

public enum JoinType {
    INNER_JOIN(1,"inner join"),
    RIGHT_JOIN(2, "right join"),
    LEFT_JOIN(3, "left join"),;

    private final int code;
    private final String command;

    JoinType(int code, String command) {
        this.code = code;
        this.command = command;
    }

    public int getCode() {
        return this.code;
    }

    public String getCommand() {
        return this.command;
    }
}
