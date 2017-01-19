package cocaine;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import cocaine.session.Session;

import java.util.Map;

/**
 * @author metal
 */
@CocaineService("tvm")
public interface Tvm extends AutoCloseable {
    @CocaineMethod("ticket")
    Session<String> ticket(String grantType, Map<String, String> options);

    @Override
    void close();
}
