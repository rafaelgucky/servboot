package net.servboot.test;

import net.servboot.sets.DataSet;

public class PersonService {
    private final DataSet<Person> dataSet = new DataSet<>(Person.class);
}
