package net.servboot.orm;

import net.servboot.utils.reflection.ReflectionUtils;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ModelIterator<T> implements Iterable<T>, Closeable {
    private final Class<T> clazz;
    private final ResultSet resultSet;
    private DataSet<T> dataSet;
    private List<String> queriedColumns;

    public ModelIterator(Class<T> clazz, ResultSet resultSet, DataSet<T> dataSet) {
        this(clazz, resultSet);
        this.dataSet = dataSet;
    }

    public ModelIterator(Class<T> clazz, ResultSet resultSet) {
        this.clazz = clazz;
        this.resultSet = resultSet;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    if (!resultSet.next()) {
                        close();
                        return false;
                    }

                    return true;
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
                    dataSet.add(model);

                    return model;
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    @Override
    public void close() throws IOException {
        try {
            this.resultSet.getStatement().close();
            this.resultSet.close();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public boolean isLast() {
        try {
            return this.resultSet.isLast();
        } catch (SQLException ignore) {
            return true;
        }
    }

    public List<T> toList() {
        List<T> objects = new LinkedList<>();
        forEach(objects::add);

        return objects;
    }
}
