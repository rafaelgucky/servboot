import net.servboot.Application;
import net.servboot.service.PersonService;

public class Main {
    public static void main(String[] args) {
        Application application = new Application();
        application.addApplicationScoped(PersonService.class);
        application.run();
    }
}
