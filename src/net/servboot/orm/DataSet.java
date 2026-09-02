package net.servboot.orm;

import net.servboot.orm.enums.Operator;
import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.reflect.Field;
import java.util.*;

public class DataSet<T> extends LinkedHashSet<T> {
    private int limit;
    private final Class<T> entityClass;
    private final Select<T> select;
    private final List<Join> joins;
    private List<Condition> conditions;
    private List<Group> groups;
    private List<Order> orders;

    public DataSet(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.select = new Select<>(entityClass);
        this.joins = OrmReflectionUtils.getJoins(entityClass);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
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

    public List<Group> getGroups() {
        return this.groups;
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataSet<T> clone() {
        DataSet<T> clone = (DataSet<T>) super.clone();
        clone.reset();
        return clone;
    }

    @Override
    public boolean add(T t) {
        if (!contains(t)) {
            return super.add(t);
        }

        return false;
    }

    @Override
    public boolean contains(Object o) {
        try {
            for (T entity : this) {
                if (OrmReflectionUtils.equals(entity, o)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return false;
    }

    public void addCondition(List<Condition> conditions) {
        if (this.conditions == null) {
            this.conditions = conditions;
        } else {
            this.conditions.addAll(conditions);
        }
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

    public void addGroup(List<Group> groups) {
        if (this.groups == null) {
            this.groups = groups;
        }  else {
            this.groups.addAll(groups);
        }
    }

    public void addGroup(Group group) {
        if (this.groups == null) {
            this.groups = new LinkedList<>();
        }

        this.groups.add(group);
    }

    public void clearGroups() {
        if (this.groups != null) {
            this.groups.clear();
        }
    }

    public void addOrder(List<Order> orders) {
        if (this.orders == null) {
            this.orders = orders;
        }  else {
            this.orders.addAll(orders);
        }
    }

    public void addOrder(Order order) {
        if (this.orders == null) {
            this.orders = new LinkedList<>();
        }

        this.orders.add(order);
    }

    public void clearOrder() {
        if (this.orders != null) {
            this.orders.clear();
        }
    }

    public void reset() {
        this.clearConditions();
        this.clearGroups();
        this.clearOrder();
    }

    public String getCommand() {
        return this.select.getCommand() +
                Join.getCommand(this.joins) +
                Condition.getCommand(this.conditions) +
                Group.getCommand(this.groups) +
                Order.getCommand(this.orders) +
                (this.limit > 0 ? " limit " + this.limit : "");
    }

    public DataSet<T> filter(String field, String operator, Object value) {
        try {
            Field classField = ReflectionUtils.getField(this.entityClass, field);
            Condition condition = new Condition((OrmReflectionUtils.getTableName(classField.getDeclaringClass()) + "." + OrmReflectionUtils.getDbFieldName(classField)), Operator.of(operator), value);
            if (this.conditions != null && !this.conditions.isEmpty()) {
                condition.setSQLOperator(Operator.AND);
            }
            this.addCondition(condition);
        } catch (NoSuchFieldException ignore) { }

        return this;
    }

    public ModelIterator<T> findAsIterable() {
        return Query.executeQuery(this.getCommand(), resultSet -> {
            return new ModelIterator<>(entityClass, resultSet, this);
        });
    }

    public List<T> find() {
        return findAsIterable().toList();
    }

    public void fillAsync() {

    }
}