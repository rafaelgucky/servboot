package net.servboot.orm;

public class ColumnMap {
    private String dbColumnName;
    private String entityFieldName;

    public ColumnMap(String dbColumnName, String entityFieldName) {
        this.dbColumnName = dbColumnName;
        this.entityFieldName = entityFieldName;
    }

    public String getDbColumnName() {
        return dbColumnName;
    }

    public void setDbColumnName(String dbColumnName) {
        this.dbColumnName = dbColumnName;
    }

    public String getEntityFieldName() {
        return entityFieldName;
    }

    public void setEntityFieldName(String entityFieldName) {
        this.entityFieldName = entityFieldName;
    }
}
