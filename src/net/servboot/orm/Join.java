package net.servboot.orm;

import net.servboot.orm.enums.JoinType;
import java.util.LinkedList;
import java.util.List;

public class Join {
    private JoinType type;
    private String table;
    private List<Condition> conditions;

    public Join() {
        this.conditions = new LinkedList<>();
    }

    public Join(JoinType type, String table) {
        this.type = type;
        this.table = table;
        this.conditions = new LinkedList<>();
    }

    public Join(JoinType type, String table, List<Condition> conditions) {
        this.type = type;
        this.table = table;
        this.conditions = conditions;
    }

    public JoinType getType() {
        return type;
    }

    public void setType(JoinType type) {
        this.type = type;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }


    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    public String getCommand() {
        StringBuilder command = new StringBuilder();

        command.append(this.getType().getCommand());
        command.append(" ");
        command.append(this.getTable());
        command.append(" on ");
        command.append(this.getConditions().getFirst().getCommand(false));

        for (int i = 1; i < this.getConditions().size(); i++) {
            command.append(this.getConditions().get(i).getCommand());
        }

        return command.toString();
    }
}
