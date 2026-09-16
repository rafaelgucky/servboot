package net.servboot.context;

import net.servboot.orm.DataSet;
import net.servboot.test.Person;
import net.servboot.test.User;

public class DataBaseContext {
    public DataSet<Person> personDataSet = new DataSet<>(Person.class);
    public DataSet<User> userDataSet = new DataSet<>(User.class);

    public DataSet<Person> getPersonDataSet() {
        return this.personDataSet;
    }

    public DataSet<User> getUserDataSet() {
        return this.userDataSet;
    }
}
