import net.servboot.Application;
import net.servboot.test.PersonService;

public class Main {
    public static void main(String[] args) {
        try {
            Application application = new Application(10);
            application.addRequestScoped(PersonService.class);
            application.init();
            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
