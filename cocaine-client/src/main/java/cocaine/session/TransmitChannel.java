package cocaine.session;

import cocaine.api.TransactionTree;

/**
 * @author akirakozov
 */
public class TransmitChannel {
    private final TransactionTree transmitTree;

    public TransmitChannel(TransactionTree transmitTree) {
        this.transmitTree = transmitTree;
    }

    public void onCompleted() {
        // TODO: implement
    }
}
