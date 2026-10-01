package net.servboot.orm;

import net.servboot.orm.enums.JoinType;
import net.servboot.utils.strings.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class Join {
    private JoinType type;
    private String table;
    private String alias;
    private Class<?> entityType;
    private Join parentJoin;
    private List<Condition> conditions;

    public Join() {
        this.conditions = new LinkedList<>();
    }

    public Join(Class<?> entityType, JoinType type, String table, String alias, Join parentJoin) {
        this.entityType = entityType;
        this.type = type;
        this.table = table;
        this.alias = alias;
        this.parentJoin = parentJoin;
        this.conditions = new LinkedList<>();
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(Class<?> entityType) {
        this.entityType = entityType;
    }

    public Join getParentJoin() {
        return parentJoin;
    }

    public void setParentJoin(Join parentJoin) {
        this.parentJoin = parentJoin;
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

    public String getAliasParent() {
        return this.getParentJoin() != null ? this.getParentJoin().getAlias() : null;
    }

    public String getPropertyName() {
        String entityFieldName = "";
        char[] characters = getAlias().toCharArray();

        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == '"') {
                continue;
            }

            if (characters[i] == '_') {
                entityFieldName += "." + Character.toUpperCase(characters[++i]);
            } else {
                entityFieldName += characters[i];
            }
        }

        return StringUtils.upperFirst(entityFieldName);
    }

    public String getCommand() {
        StringBuilder command = new StringBuilder();

        command.append(this.getType().getCommand());
        command.append(" ");
        command.append(this.getTable());
        command.append(this.getAlias() != null ? " as " + this.getAlias() : "");
        command.append(" on ");
        command.append(this.getConditions().getFirst().getCommand(false));

        for (int i = 1; i < this.getConditions().size(); i++) {
            command.append(this.getConditions().get(i).getCommand());
        }

        return command.toString();
    }

    public static String getCommand(List<Join> joins) {
        if (joins == null || joins.isEmpty()) return " ";

        return joins.stream()
                .map((join) -> StringUtils.concat(join.getCommand(), "\n"))
                .reduce(StringUtils::concat)
                .get();
    }
}
