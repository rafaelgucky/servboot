package net.servboot.test;

import net.servboot.context.DataBaseContext;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.DataSet;
import net.servboot.orm.ModelIterator;
import net.servboot.orm.Query;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonService implements IService<Person> {

    public static Class<?> getClassStatically() {
        return MethodHandles.lookup().lookupClass();
    }

    public ModelIterator<Person> findAll() throws SQLException, InterruptedException {
        Statement stmt = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(DataBaseContext.getPersonDataSet().getCommand());
        return new ModelIterator<>(Person.class, rs);
    }

    public Person findById(int id) throws InterruptedException, SQLException {
        Person person = new Person();
        DataSet<Person> dataSet = DataBaseContext.getPersonDataSet();
        dataSet.filter("id", "=", id);

        Query.executeQuery(dataSet.getCommand(), (resultSet) -> {
            try {
                resultSet.next();
                OrmReflectionUtils.fillEntityFromResultSet(person, resultSet);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return person;
    }
}
