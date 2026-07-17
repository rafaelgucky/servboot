package net.servboot.sets;

import java.util.LinkedList;

public class DataSet<T> extends LinkedList<T> {
    private final Class<T> entityClass;

    public DataSet(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

}