package net.servboot.test;

import net.servboot.orm.DataSet;

public class PersonService {
    private final DataSet<Person> dataSet = new DataSet<>(Person.class);
}
