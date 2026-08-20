package net.servboot.test;

import net.servboot.orm.ModelIterator;

public interface IService <T> {
    ModelIterator<T> findAll() throws Exception;
    T findById(int id) throws Exception;
}
