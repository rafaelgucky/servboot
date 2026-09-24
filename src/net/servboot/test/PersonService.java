package net.servboot.test;

import net.servboot.orm.context.DataSet;
import net.servboot.orm.ModelIterator;
import net.servboot.orm.Query;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

public class PersonService implements IService<Person> {

    private DataSet<Person> personDataSet;

    public void setPersonDataSet(DataSet<Person> personDataSet) {
        this.personDataSet = personDataSet;
    }

    public static Class<?> getClassStatically() {
        return MethodHandles.lookup().lookupClass();
    }

    public ModelIterator<Person> findAll() throws SQLException, InterruptedException {
        return Query.executeQuery(personDataSet.getCommand(), (resultSet) -> {
            return new ModelIterator<>(Person.class, resultSet);
        });
    }

    public Person findById(int id) throws InterruptedException, SQLException {
        Person person = new Person();
        personDataSet.filter("id", "=", id);

        Query.executeQuery(personDataSet.getCommand(), (resultSet) -> {
            resultSet.next();
            OrmReflectionUtils.fillEntityFromResultSet(person, resultSet);
        });

        return person;
    }
}
