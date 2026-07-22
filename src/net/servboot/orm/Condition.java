package net.servboot.orm;

import net.servboot.orm.enums.Operator;

import java.util.List;

public class Condition {
    private String column;
    private Operator operator;
    private Object value;
    private Operator SQLOperator;

    public Condition(String column, Operator operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    public Condition(String column, Operator operator, Object value, Operator SQLOperator) {
        this(column, operator, value);
        this.SQLOperator = SQLOperator;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Operator getOperator() {
        return this.operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Operator getSQLOperator() {
        return this.SQLOperator;
    }

    public void setSQLOperator(Operator SQLOperator) {
        this.SQLOperator = SQLOperator;
    }


    public Condition of(String column, Operator operator, Object value) {
        return new Condition(column, operator, value);
    }

    public String getCommand(boolean formatValue) {
        if (formatValue) {
            return getCommand();
        }

        return " " + (this.getSQLOperator() != null ? this.getSQLOperator().getOperator() : "") + this.getColumn() + this.getOperator().getOperator() + this.getValue().toString();
    }

    public String getCommand() {
        return " " + (this.getSQLOperator() != null ? this.getSQLOperator().getOperator() : "") + this.getColumn() + this.getOperator().getOperator() + "'" + this.getValue().toString() + "' ";
    }

    public static String getCommand(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) return " ";

        StringBuilder command = new StringBuilder();

        command.append(" WHERE ");
        for (Condition condition : conditions) {
            command.append(condition.getCommand());
        }

        return command.toString();
    }
}
