package net.servboot.context;

import net.servboot.sets.DataSet;
import net.servboot.test.Person;

public class DataBaseContext {
    public static DataSet<Person> personDataSet = new DataSet<>(Person.class);
}
