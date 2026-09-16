package net.servboot.orm;

public class InsertColumMap extends ColumnMap {
    private final Object value;

    public InsertColumMap(String dbColumnName, String entityFieldName, Object value) {
        super(dbColumnName, entityFieldName);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
