package net.servboot.context;

import net.servboot.orm.DataSet;
import net.servboot.test.Person;
import net.servboot.test.User;

public class DataBaseContext {
    public static DataSet<Person> personDataSet = new DataSet<>(Person.class);
    public static DataSet<User> userDataSet = new DataSet<>(User.class);

    public static DataSet<Person> getPersonDataSet() {
        return personDataSet.clone();
    }

    public static DataSet<User> getUserDataSet() {
        return userDataSet.clone();
    }
}
