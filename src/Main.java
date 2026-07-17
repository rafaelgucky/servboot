import net.servboot.Application;
import net.servboot.database.ConnectionManager;
import net.servboot.database.ServBootQuery;
import net.servboot.orm.Condition;
import net.servboot.orm.Join;
import net.servboot.orm.Select;
import net.servboot.orm.enums.JoinType;
import net.servboot.orm.enums.Operator;
import net.servboot.test.Person;
import net.servboot.test.PersonService;
import net.servboot.utils.reflection.ReflectionUtils;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionManager.init();
            Select<Person> q = new Select<>(Person.class);
            var x = q.getColumns();

            var c = List.of(new Condition("person.pet", Operator.EQUAL, "pet.id"));
            var j = new Join(JoinType.INNER_JOIN, "pet", c);
            var y = j.getCommand();

            String sql = q.getCommand() + j.getCommand();

            Person person = new Person();

            ReflectionUtils.callSetter(person, "Pet.id", 1);

//            var rs = ConnectionManager.getConnection().createStatement().executeQuery(sql);
//            rs.next();
//            var z = rs.getObject(1);

            Application application = new Application();
            application.addRequestScoped(PersonService.class);
            application.init();
            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
