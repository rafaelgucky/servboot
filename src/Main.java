import net.servboot.database.ConnectionManager;
import net.servboot.dependency.DependencyInjectionContainer;
import net.servboot.routing.Route;
import net.servboot.routing.RouterManager;
import net.servboot.test.PersonService;
import net.servboot.utils.strings.StringUtils;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            ConnectionManager.init();

            //Application application = new Application();
            //application.addApplicationScoped(PersonService.class);
            //application.run();
            DependencyInjectionContainer.addRequestScoped(PersonService.class);

            StringUtils.equalsIgnorePathParams("/person/find/{id}/", "/person/find/1/?name=teste");
            RouterManager.init();

            Object controller = RouterManager.getController("/person/find/1/?name=teste");
            Route route = RouterManager.getRoute("/person/find/1/?name=teste&lastname=burger");

            Map<String, String> pathParameters = StringUtils.getPathParameters("/person/find/{id}/{name}/teste/{lastname}/gucky", "/person/find/1/rafael/teste/gucky?name=teste");
            Map<String, String> queryParameters = StringUtils.getQueryParameters("/person/find/1/rafael/teste/gucky?name=teste&x=1&id=1&lastName=burger");
            Object result = route.call(8);
            int x = 1;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
