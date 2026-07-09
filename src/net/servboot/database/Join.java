package net.servboot.database;

import java.util.LinkedList;
import java.util.List;

public class Join {
    private String table;
    private int limit;
    private int offset;
    private List<String> select;
    private List<String> conditions;
    private List<String> joins;
    private List<String> group;
    private List<String> orders;

    public Join(String table) {
        this.table = table;
        this.select = new LinkedList<>();
        this.conditions = new LinkedList<>();
        this.joins = new LinkedList<>();
        this.group = new LinkedList<>();
        this.orders = new LinkedList<>();
    }

    public String getTable() {
        return table;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getSelect() {
        return select;
    }

    public void setSelect(List<String> select) {
        this.select = select;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public List<String> getJoins() {
        return joins;
    }

    public void setJoins(List<String> joins) {
        this.joins = joins;
    }

    public List<String> getOrders() {
        return orders;
    }

    public void setOrders(List<String> order) {
        this.orders = order;
    }

    public List<String> getGroup() {
        return group;
    }

    public void setGroup(List<String> group) {
        this.group = group;
    }

    public void addCondition(String condition){
        this.conditions.add(condition);
    }

    public void addJoin(String join){
        this.joins.add(join);
    }

    public void addSelect(String column){
        this.select.add(column);
    }

    public void addGroup(String group){
        this.group.add(group);
    }

    public void addOrder(String order){
        this.orders.add(order);
    }

    public String generateSql(){
        StringBuilder sql = new StringBuilder();

        // Select
        sql.append("SELECT ");
        for(int i = 0; i < select.size(); i++){
            sql.append(select.get(i));
            sql.append(i < select.size() - 1 ? ", " : " ");
        }
        sql.append(" FROM ").append(table);

        // Joins
        if(!this.joins.isEmpty()){
            for(String join : this.joins){
                sql.append(join);
            }
        }

        // Conditions
        if(!this.conditions.isEmpty()){
            sql.append(" WHERE ");
            for(int i = 0; i < conditions.size(); i++){
                sql.append(conditions.get(i));
                sql.append(i < conditions.size() - 1 ? " AND " : " ");
            }
        }


        // Group
        if(!this.group.isEmpty()){
            sql.append(" GROUP BY ");
            for(int i = 0; i < this.group.size(); i++){
                sql.append(this.group.get(i));
                sql.append(i < this.group.size() - 1 ? ", " : " ");
            }
        }


        // Order
        if(!this.orders.isEmpty()){
            sql.append(" ORDER BY ");
            for(int i = 0; i < this.orders.size(); i++){
                sql.append(this.orders.get(i));
                sql.append(i < this.orders.size() - 1 ? ", " : " ");
            }
        }


        // Limit and offset
        if(limit > 0){
            sql.append(" LIMIT ").append(this.limit);
        }

        if(this.offset > 0){
            sql.append(" OFFSET ").append(this.offset);
        }

        sql.append(";");

        return sql.toString();
    }
}
