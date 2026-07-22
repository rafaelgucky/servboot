package net.servboot.context;

import net.servboot.orm.DataSet;
import net.servboot.test.Person;

public class DataBaseContext {
    public static DataSet<Person> personDataSet = new DataSet<>(Person.class);
}
