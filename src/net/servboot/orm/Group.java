package net.servboot.orm;

import java.util.List;

public class Group {
    public static final String baseCommand = " group by ";
    private final ColumnMap column;

    public Group(ColumnMap column) {
        this.column = column;
    }

    public String getCommand() {
        return this.column.getDbColumnName();
    }

    public static String getCommand(List<Group> groups) {
        if (groups == null || groups.isEmpty()) return " ";

        StringBuilder command = new StringBuilder();

        command.append(baseCommand);
        for (int i = 0; i < groups.size(); i++) {
            command.append(groups.get(i).getCommand());
            command.append(i == groups.size() - 1 ? " " : ", ");
        }

        return command.toString();
    }
}
