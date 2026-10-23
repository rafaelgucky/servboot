import net.servboot.Application;
import net.servboot.orm.context.DataBaseContext;
import net.servboot.orm.database.ConnectionManager;
import net.servboot.orm.Query;
import net.servboot.test.service.PersonService;
import net.servboot.utils.reflection.ReflectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        try {
            Application application = new Application(100);
            application.addRequestScoped(PersonService.class);
            application.addRequestScoped(DataBaseContext.class);
            application.init();
            application.setLogger(Main::onException);

            System.out.println("ServBootORM: Loading connections...");
            ConnectionManager.init();
            System.out.println("ServBootORM: Connections loaded!");

            application.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void onException(Throwable ex) {
        try {
            ConnectionManager.rollback();
            ConnectionManager.begin();
            if (Arrays.stream(ex.getClass().getMethods()).anyMatch(m -> m.getName().equalsIgnoreCase("getTargetException"))) {
                ex = ReflectionUtils.callGetter(ex, "targetException");
            }
            String stackTrace = Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1  + "\r\n" + s2).orElse("");
            Query.executePreparedUpdate("insert into eventer.errorlogs (message, stacktrace) values (?, ?);", List.of(Objects.requireNonNullElse(ex.getMessage(), ""), stackTrace));
            ConnectionManager.commit();

            ex.printStackTrace();
        } catch (Exception ignore) { }
    }
}
