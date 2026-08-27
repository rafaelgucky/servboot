package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ModelIterator<T> implements Iterable<T> {
    private final Class<T> clazz;
    private final ResultSet resultSet;
    private List<String> queriedColumns;

    public ModelIterator(Class<T> clazz, ResultSet resultSet) {
        this.clazz = clazz;
        this.resultSet = resultSet;
    }

    public boolean isLast() {
        try {
            return this.resultSet.isLast();
        } catch (SQLException ignore) {
            return true;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    if (!resultSet.next()) {
                        resultSet.getStatement().close();
                        return false;
                    }

                    return true;
                } catch (SQLException sqlException) {
                    throw new RuntimeException(sqlException);
                }
            }

            @Override
            public T next() {
                try {
                    if (queriedColumns == null) {
                        queriedColumns = OrmReflectionUtils.getQueriedColumns(resultSet);
                    }

                    T model = ReflectionUtils.instantiate(clazz, false);
                    OrmReflectionUtils.fillEntityFromResultSet(model, resultSet, queriedColumns);
                    return model;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    public List<T> toList() {
        List<T> objects = new LinkedList<>();
        forEach(objects::add);

        return objects;
    }
}
