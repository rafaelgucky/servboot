package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ModelIterator<T> implements Iterator<T> {
    private final Class<T> clazz;
    private final ResultSet resultSet;
    private final List<String> queriedColumns;

    public ModelIterator(Class<T> clazz, ResultSet resultSet) {
        this.clazz = clazz;
        this.resultSet = resultSet;
        this.queriedColumns = OrmReflectionUtils.getQueriedColumns(this.resultSet);
    }

    @Override
    public boolean hasNext() {
        try {
            return this.resultSet.next();
        } catch (SQLException ignore) {
            return false;
        }
    }

    @Override
    public T next() {
        try {
            T model = ReflectionUtils.instantiate(clazz, false);
            OrmReflectionUtils.fillEntityFromResultSet(model, this.resultSet, this.queriedColumns);
            return model;
        } catch (Exception ignore) {
            return null;
        }
    }

    public boolean isLast() {
        try {
            return this.resultSet.isLast();
        } catch (SQLException ignore) {
            return true;
        }
    }
}
