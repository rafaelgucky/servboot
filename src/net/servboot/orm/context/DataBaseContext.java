package net.servboot.orm.context;

import net.servboot.test.model.Event;
import net.servboot.test.model.Person;
import net.servboot.test.model.User;

public class DataBaseContext extends DbContext {
    private final DataSet<Person> personDataSet = new DataSet<>(Person.class);
    private final DataSet<User> userDataSet = new DataSet<>(User.class);
    private final DataSet<Event> eventDataSet = new DataSet<>(Event.class);

    public DataSet<Person> getPersonDataSet() {
        return this.personDataSet;
    }

    public DataSet<User> getUserDataSet() {
        return this.userDataSet;
    }

    public DataSet<Event> getEventDataSet() {
        return this.eventDataSet;
    }
}
