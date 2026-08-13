import net.servboot.Application;
import net.servboot.context.DataBaseContext;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.DataSet;
import net.servboot.test.Person;
import net.servboot.test.PersonService;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionManager.init();

            DataSet<Person> pdt = DataBaseContext.getPersonDataSet();

            String sql = pdt.getCommand();
            Connection connection = ConnectionManager.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sql);

            List<Person> ps = OrmReflectionUtils.getAllEntitiesByResultSet(Person.class, resultSet);
            //OrmReflectionUtils.fillEntityFromResultSetTest(p,  resultSet);

            ps = ps.stream()
                    .filter(person -> person.getPets() != null)
                    .toList();

            Application application = new Application(10);
            application.addRequestScoped(PersonService.class);
            application.init();
            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
