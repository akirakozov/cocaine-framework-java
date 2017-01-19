package cocaine.service.invocation;

import cocaine.hpack.HeaderField;

import java.util.List;
import java.util.function.Function;

/**
 * @author metal
 */
public abstract class AdditionalHeadersAppender implements Function<List<HeaderField>, List<HeaderField>> {
}
