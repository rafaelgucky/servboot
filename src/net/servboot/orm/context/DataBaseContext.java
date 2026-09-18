package net.servboot.orm.context;

import net.servboot.test.Person;
import net.servboot.test.User;

public class DataBaseContext extends DbContext {
    private final DataSet<Person> personDataSet = new DataSet<>(Person.class);
    private final DataSet<User> userDataSet = new DataSet<>(User.class);

    public DataSet<Person> getPersonDataSet() {
        return this.personDataSet;
    }

    public DataSet<User> getUserDataSet() {
        return this.userDataSet;
    }
}
