package net.servboot.orm;

import net.servboot.orm.enums.Operator;

import java.util.List;

public class Order {
    public static final String baseCommand = " order by ";
    private final ColumnMap column;
    private final Operator operator;

    public Order(ColumnMap column) {
        this.column = column;
        this.operator = Operator.ASC;
    }

    public Order(ColumnMap column, Operator operator) {
        this.column = column;
        this.operator = operator;
    }

    public String getCommand() {
        return this.column.getDbColumnName() + this.operator.getOperator();
    }

    public static String getCommand(List<Order> orders) {
        if (orders == null || orders.isEmpty()) return " ";

        StringBuilder command = new StringBuilder();

        command.append(baseCommand);
        for (int i = 0; i < orders.size(); i++) {
            command.append(orders.get(i).getCommand());
            command.append(i == orders.size() - 1 ? " " : ", ");
        }

        return command.toString();
    }
}
