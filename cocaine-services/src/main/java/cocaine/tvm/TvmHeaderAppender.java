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
        headers.add(new HeaderField(
                "authorization".getBytes(),
                tvmTicketHolder.getCurrentTvmTicket().getTicket().getBytes()));
        return headers;
    }
}
