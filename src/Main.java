import net.servboot.Application;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.Condition;
import net.servboot.orm.enums.Operator;
import net.servboot.sets.DataSet;
import net.servboot.test.Person;
import net.servboot.test.PersonService;
import net.servboot.utils.reflection.orm.OrmReflectionUtils;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
//            ConnectionManager.init();
//
//            DataSet<Person> dataSet = new DataSet<>(Person.class);
//            dataSet.addCondition(new Condition("1", Operator.EQUAL, "0"));
//
//            var rs = ConnectionManager.getConnection().createStatement().executeQuery(dataSet.getSelectCommand());
//
//            List<Person> persons = OrmReflectionUtils.getAllEntitiesFromResultSet(Person.class, rs);



            Application application = new Application(100);
            application.addRequestScoped(PersonService.class);
            application.init();
            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
