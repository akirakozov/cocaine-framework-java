package cocaine.tvm;

import cocaine.hpack.HeaderField;
import cocaine.service.invocation.AdditionalHeadersAppender;

import java.util.List;

/**
 * @author metal
 */
public class TvmHeaderAppender extends AdditionalHeadersAppender {
    private final TvmTicketHolder tvmTicketHolder;

    public TvmHeaderAppender(TvmTicketHolder tvmTicketHolder) {
        this.tvmTicketHolder = tvmTicketHolder;
    }

    @Override
    public List<HeaderField> apply(List<HeaderField> headers) {
        TvmTicket currentTvmTicket = tvmTicketHolder.getCurrentTvmTicket();
        if (currentTvmTicket != null) {
            headers.add(new HeaderField("authorization".getBytes(), currentTvmTicket.getTicket().getBytes()));
        }
        return headers;
    }
}
