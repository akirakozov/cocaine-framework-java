package cocaine.log4j;

import cocaine.Logging;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

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
        delegate.append(event.getLevel(), sourceName, message);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

}
