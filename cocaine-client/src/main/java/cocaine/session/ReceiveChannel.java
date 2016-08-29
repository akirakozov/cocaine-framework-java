package cocaine.session;

import cocaine.ServiceException;
import cocaine.UnexpectedServiceMessageException;
import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import cocaine.session.protocol.CocaineProtocol;
import cocaine.session.protocol.IdentityProtocol;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author akirakozov
 */
public class ReceiveChannel<T> {
    private static final Logger logger = Logger.getLogger(ReceiveChannel.class);

    private TransactionTree rxTree;
    private final BlockingQueue<ResultMessage> queue;
    private final CocaineProtocol protocol;
    private final CocainePayloadDeserializer<T> deserializer;
    private final String serviceName;
    private final long readTimeout;

    private boolean completed = false;
    private boolean hasError = false;

    public ReceiveChannel(
            String serviceName,
            TransactionTree rxTree,
            CocaineProtocol protocol,
            CocainePayloadDeserializer<T> deserializer,
            long readTimeout)
    {
        this.serviceName = serviceName;
        this.rxTree = rxTree;
        this.queue = new LinkedBlockingQueue<>();
        this.protocol = protocol;
        this.deserializer = deserializer;
        this.readTimeout = readTimeout;
    }

    public T get() {
        if (protocol instanceof IdentityProtocol) {
            onCompleted();
            return null;
        }

        PayloadResultMessage msg = getNextPayloadMessage();
        Value payload = protocol.handle(serviceName, msg.messageType, msg.payload);
        try {
            if (payload != null) {
                return deserializer.deserialize(msg.messageType, payload);
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(
                    "Couldn't deserialize result of message " + msg.messageType + ", " + e.getMessage(), e);
            throw new ServiceException(serviceName, e.getMessage());
        }
    }

    void onRead(int type, Value payload) {
        Optional<TransactionInfo> info = rxTree.getInfo(type);
        if (!info.isPresent()) {
            putMessageInQueue(new ErrorResultMessage(new UnexpectedServiceMessageException(serviceName, type)));
            logger.error("Unknown message type: " + type + ", for service " + serviceName);
        } else {
            putMessageInQueue(new PayloadResultMessage(info.get().getMessageName(), payload));

            TransactionTree tree = info.get().getTree();
            if (!tree.isCycle()) {
                if (tree.isEmpty()) {
                    onCompleted();
                    logger.info("Last message received");
                } else {
                    rxTree = tree;
                }
            }
        }
    }

    private PayloadResultMessage getNextPayloadMessage() {
        try {
            if (completed && queue.isEmpty()) {
                throw new ServiceException(serviceName, "Read channel is completed and has empty queue");
            }

            ResultMessage nextMessage = pollTheQueue();
            if (nextMessage.isErrorMessage()) {
                Exception error = ((ErrorResultMessage) nextMessage).error;
                throw new ServiceException(serviceName, error.getClass().getName() + ": " + error.getMessage());
            } else {
                return (PayloadResultMessage) nextMessage;
            }
        } catch (InterruptedException e) {
            throw new ServiceException(serviceName, "Reading interrupted, " + e.getMessage());
        }
    }

    private ResultMessage pollTheQueue() throws InterruptedException {
        if (readTimeout == 0) {
            return queue.take();
        } else {
            ResultMessage result = queue.poll(readTimeout, TimeUnit.MILLISECONDS);
            if (result == null) {
                throw new ServiceException(serviceName,
                        "Read timeout occurred in receive channel, timeout = " + readTimeout + " ms");
            }
            return result;
        }
    }

    private void putMessageInQueue(ResultMessage resultMessage) {
        try {
            if (!completed && !hasError) {
                queue.put(resultMessage);
            }
            hasError = hasError || resultMessage.isErrorMessage();
        } catch (InterruptedException e) {
            throw new ServiceException(serviceName, "Putting message in queue interrupted, " + e.getMessage());
        }
    }

    public void onCompleted() {
        completed = true;
    }

    private static abstract class ResultMessage {
        public abstract boolean isErrorMessage();
    }

    private static class PayloadResultMessage extends ResultMessage {
        private String messageType;
        private Value payload;

        public PayloadResultMessage(String messageType, Value payload) {
            this.messageType = messageType;
            this.payload = payload;
        }

        @Override
        public boolean isErrorMessage() {
            return false;
        }
    }

    private static class ErrorResultMessage extends ResultMessage {
        private Exception error;

        public ErrorResultMessage(Exception error) {
            this.error = error;
        }

        @Override
        public boolean isErrorMessage() {
            return true;
        }
    }
}
