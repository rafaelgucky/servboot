import net.servboot.Application;
import net.servboot.database.ConnectionManager;
import net.servboot.test.Person;
import net.servboot.test.PersonService;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionManager.init();

            String sql = "select person.id as id, person.name as name, person.age as age, person.lastName as lastName, pet.id as \"Pet.id\", pet.name as \"Pet.name\" from orm.person left join orm.pet on pet.personId = person.id /*where person.id = 10;*/";
            Connection connection = ConnectionManager.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sql);

            List<Person> ps = OrmReflectionUtils.getAllEntitiesByResultSet(Person.class, resultSet);
            //OrmReflectionUtils.fillEntityFromResultSetTest(p,  resultSet);

            ps = ps.stream()
                    .filter(person -> person.getPets() != null)
                    .toList();

            System.exit(0);
            Application application = new Application(10);
            application.addRequestScoped(PersonService.class);
            application.init();
            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
