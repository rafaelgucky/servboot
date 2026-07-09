import net.servboot.Application;
import net.servboot.database.ConnectionManager;
import net.servboot.routing.RouterManager;
import net.servboot.test.PersonService;

public class Main {
    public static void main(String[] args) {
        try {
            //ConnectionManager.init();

            //Application application = new Application();
            //application.addApplicationScoped(PersonService.class);
            //application.run();
            RouterManager.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
