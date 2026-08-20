package net.servboot.test;

import net.servboot.context.DataBaseContext;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.DataSet;
import net.servboot.orm.ModelIterator;
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

    public Person findById(int id)
            throws InterruptedException, SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Person person = new Person();

        try (
            Statement stmt = ConnectionManager.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ) {
            DataSet<Person> dataSet = DataBaseContext.getPersonDataSet();
            dataSet.filter("id", "=", id);
            ResultSet rs = stmt.executeQuery(dataSet.getCommand());
            rs.next();
            OrmReflectionUtils.fillEntityFromResultSet(person, rs);
        } catch (InterruptedException|SQLException|NoSuchFieldException|IllegalAccessException|InvocationTargetException|InstantiationException e) {
            throw e;
        }

        return person;
    }
}
