package cocaine.tvm;

import cocaine.hpack.HeaderField;

import java.util.List;
import java.util.function.Function;

/**
 * @author metal
 */
public class TvmHeaderAppender {
    public static Function<List<HeaderField>, List<HeaderField>> appendTvmHeaderF(TvmTicketHolder tvmTicketHolder) {
        return (List<HeaderField> headers) -> {
            headers.add(new HeaderField(
                    "authorization".getBytes(),
                    tvmTicketHolder.getCurrentTvmTicket().getTicket().getBytes()));
            return headers;
        };
    }
}
