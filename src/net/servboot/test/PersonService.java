package net.servboot.test;

import net.servboot.context.DataBaseContext;
import net.servboot.orm.DataSet;
import net.servboot.orm.ModelIterator;
import net.servboot.orm.Query;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;

public class PersonService implements IService<Person> {

    public static Class<?> getClassStatically() {
        return MethodHandles.lookup().lookupClass();
    }

    public ModelIterator<Person> findAll() throws SQLException, InterruptedException {
        return Query.executeQuery(DataBaseContext.getPersonDataSet().getCommand(), (resultSet) -> {
            return new ModelIterator<>(Person.class, resultSet);
        });
    }

    public Person findById(int id) throws InterruptedException, SQLException {
        Person person = new Person();
        DataSet<Person> dataSet = DataBaseContext.getPersonDataSet();
        dataSet.filter("id", "=", id);

        Query.executeQuery(dataSet.getCommand(), (resultSet) -> {
            resultSet.next();
            OrmReflectionUtils.fillEntityFromResultSet(person, resultSet);
        });

        return person;
    }
}
