package cocaine.tvm;

import cocaine.Tvm;

/**
 * @author metal
 */
public class TvmTicketHolder {
    private static final String TVM_TOKEN_TYPE_KEY = "TVM";
    private static final String TOKEN_TYPE_KEY = "COCAINE_APP_TOKEN_TYPE";
    private static final String TOKEN_BODY_KEY = "COCAINE_APP_TOKEN_BODY";

    private TvmTicket current = null;
    private final Tvm tvm;
    private boolean enabled = false;

    public TvmTicketHolder(Tvm tvm, boolean enabled) {
        this.tvm = tvm;
        this.enabled = enabled;

        if (enabled) {
            String tokenType = System.getenv(TOKEN_TYPE_KEY);
            if (TVM_TOKEN_TYPE_KEY.equals(tokenType)) {
                this.current = new TvmTicket(System.getenv(TOKEN_BODY_KEY));
            }
        }
    }

    public TvmTicket getCurrentTvmTicket() {
        return current;
    }

    public void updateTicket() {
        if (enabled) {
            current = new TvmTicket(tvm.ticket(current.getGrantType(), current.getOptions()).rx().get());
        }
    }
}
