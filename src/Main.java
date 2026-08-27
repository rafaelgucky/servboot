import net.servboot.Application;
import net.servboot.database.ConnectionManager;
import net.servboot.orm.Query;
import net.servboot.test.PersonService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        try {
            Application application = new Application(100);
            application.addRequestScoped(PersonService.class);
            application.init();
            application.setLogger(Main::onException);
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
                ex = (Throwable) ex.getClass().getMethod("getTargetException").invoke(ex);
            }
            String stackTrace = Arrays.stream(ex.getStackTrace()).map(StackTraceElement::toString).reduce((s1, s2) -> s1  + "\r\n" + s2).orElse("");
            Query.executePreparedUpdate("insert into eventer.errorlogs (message, stacktrace) values (?, ?);", List.of(Objects.requireNonNullElse(ex.getMessage(), ""), stackTrace));
            ConnectionManager.commit();
        } catch (Exception ignore) { }
    }
}
