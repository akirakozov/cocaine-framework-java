package cocaine.tvm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author metal
 */
public class TvmTicket {
    private final String grantType;
    private final Map<String, String> parameters;

    public TvmTicket(String grantType, Map<String, String> parameters) {
        this.grantType = grantType;
        this.parameters = parameters;
    }

    public TvmTicket(String ticketOption) {
        this.grantType = TicketGrantTypes.TICKET.name().toLowerCase();

        Map<String, String> options = new HashMap<>();
        options.put(TicketOptions.TICKET.name().toLowerCase(), ticketOption);
        this.parameters = options;
    }

    public String getGrantType() {
        return grantType;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getTicket() {
        return parameters.get(TicketOptions.TICKET.name().toLowerCase());
    }

    public enum TicketGrantTypes {
        TICKET, OAUTH
    }

    public enum TicketOptions {
        TICKET, USERIP, OAUTH_TOKEN
    }
}
