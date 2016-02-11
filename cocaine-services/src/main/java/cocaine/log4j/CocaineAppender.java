package cocaine.log4j;

import cocaine.Logging;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class CocaineAppender extends AppenderSkeleton {

    private final Logging delegate;
    private final String sourceName;

    public CocaineAppender(String sourceName, Logging delegate) {
        this.delegate = delegate;
        this.sourceName = sourceName;
    }

    @Override
    protected void append(LoggingEvent event) {
        String message = getLayout().format(event);
        Map<String, Object> attrs = prepareExtraAttributes();
        if (attrs.isEmpty()) {
            delegate.append(event.getLevel(), sourceName, message);
        } else {
            delegate.append(event.getLevel(), sourceName, message, packAttributes(attrs));
        }
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    protected Map<String, Object> prepareExtraAttributes() {
        return Collections.EMPTY_MAP;
    }

    private List<List<String>> packAttributes(Map<String, Object> attrs) {
        return attrs.entrySet().stream()
                .map(e -> Arrays.asList(e.getKey(), e.getValue().toString()))
                .collect(Collectors.toList());
    }

}
