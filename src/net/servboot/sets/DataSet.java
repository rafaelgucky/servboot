package net.servboot.sets;

import net.servboot.orm.Condition;
import net.servboot.orm.Join;
import net.servboot.orm.Select;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.util.LinkedList;
import java.util.List;

public class DataSet<T> extends LinkedList<T> implements Cloneable {
    private final Class<T> entityClass;
    private final Select<T> select;
    private final List<Join> joins;
    private List<Condition> conditions;

    public DataSet(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.select = new Select<>(entityClass);
        this.joins = OrmReflectionUtils.getJoins(entityClass);
    }

    public Class<T> getEntityClass() {
        return this.entityClass;
    }

    public Select<T> getSelect() {
        return this.select;
    }

    public List<Join> getJoins() {
        return this.joins;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }


    public void addCondition(Condition condition) {
        if (this.conditions == null) {
            this.conditions = new LinkedList<>();
        }

        this.conditions.add(condition);
    }

    public void clearConditions() {
        if (this.conditions != null) {
            this.conditions.clear();
        }
    }

    public String getSelectCommand() {
        StringBuilder command = new StringBuilder();

        command.append(select.getCommand());

        for (Join join : this.joins) {
            command.append(join.getCommand());
        }

        if (this.conditions != null) {
            command.append(" WHERE ");
            for (Condition condition : this.conditions) {
                command.append(condition.getCommand());
            }
        }

        //command.append(" limit 100");

        return command.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataSet<T> clone() {
        DataSet<T> clone = (DataSet<T>) super.clone();
        clone.clearConditions();

        return clone;
    }
}