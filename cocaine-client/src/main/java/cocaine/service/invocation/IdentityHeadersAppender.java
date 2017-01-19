package cocaine.service.invocation;

import cocaine.hpack.HeaderField;

import java.util.List;

/**
 * @author metal
 */
public class IdentityHeadersAppender extends AdditionalHeadersAppender {
    @Override
    public List<HeaderField> apply(List<HeaderField> headerFields) {
        return headerFields;
    }
}
