package cocaine.tvm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author metal
 */
public class TvmTicket {
    private final String grantType;
    private final Map<String, String> options;

    public TvmTicket(String grantType, Map<String, String> options) {
        this.grantType = grantType;
        this.options = options;
    }

    public TvmTicket(String ticketOption) {
        this.grantType = TicketGrantTypes.TICKET.name().toLowerCase();

        Map<String, String> options = new HashMap<>();
        options.put(TicketOptions.TICKET.name().toLowerCase(), ticketOption);
        this.options = options;
    }

    public String getGrantType() {
        return grantType;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getTicket() {
        return options.get(TicketOptions.TICKET.name().toLowerCase());
    }

    public enum TicketGrantTypes {
        TICKET
    }

    public enum TicketOptions {
        TICKET
    }
}
