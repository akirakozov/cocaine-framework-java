package cocaine;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import org.apache.log4j.Level;

import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
@CocaineService("logging")
public interface Logging extends AutoCloseable {

    @CocaineMethod("verbosity")
    Level getVerbosity();

    @CocaineMethod("emit")
    void append(Level level, String name, String message);

    @CocaineMethod("emit")
    void append(Level level, String name, String message, List<List<String>> attrs);

    @Override
    void close();

}
