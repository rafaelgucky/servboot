package net.servboot.orm.enums;

public enum Operator {
    AND(" and "),
    OR(" or "),
    NOT(" not "),
    EQUAL(" = "),
    NOT_EQUAL(" != "),
    LESS_THAN(" < "),
    LESS_THAN_OR_EQUAL(" <= "),
    GREATER_THAN(" > "),
    GREATER_THAN_OR_EQUAL(" >= "),
    BETWEEN(" between "),
    LIKE(" like "),
    NOT_LIKE(" not like "),
    IN(" in"),
    NOT_IN(" not in "),
    DESC(" desc "),
    ASC(" asc ");

    private final String operator;

    Operator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }
}
