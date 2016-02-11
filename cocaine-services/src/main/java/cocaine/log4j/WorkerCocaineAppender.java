package cocaine.log4j;

import cocaine.Logging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author akirakozov
 */
public class WorkerCocaineAppender extends CocaineAppender {
    private final Optional<String> uuid;

    public WorkerCocaineAppender(String sourceName, Logging delegate) {
        super(sourceName, delegate);
        this.uuid = Optional.empty();
    }

    public WorkerCocaineAppender(String sourceName, Logging delegate, String uuid) {
        super(sourceName, delegate);
        this.uuid = Optional.of(uuid);
    }

    @Override
    protected Map<String, Object> prepareExtraAttributes() {
        Map<String, Object> attrs = Collections.EMPTY_MAP;
        if (uuid.isPresent()) {
            attrs = new HashMap<>();
            attrs.put("uuid", uuid.get());
        }
        return attrs;
    }

}
