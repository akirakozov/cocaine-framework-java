package cocaine.session;

import cocaine.ServiceException;
import cocaine.UnknownServiceMethodException;
import cocaine.api.TransactionTree;
import cocaine.service.InvocationUtils;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class TransmitChannel {
    private static final Logger logger = Logger.getLogger(TransmitChannel.class);

    private final long sessionId;
    private final Channel channel;
    private final String serviceName;
    private TransactionTree txTree;
    private final AtomicBoolean isDone;

    public TransmitChannel(String serviceName, TransactionTree txTree, Channel channel, long sessionId) {
        this.serviceName = serviceName;
        this.channel = channel;
        this.sessionId = sessionId;
        this.txTree = txTree;
        this.isDone = new AtomicBoolean(false);
    }

    public void invoke(String methodType, List<Object> args) {
        checkIsDone();

        int msgId = txTree.getMessageId(methodType)
                .orElseThrow(() -> new UnknownServiceMethodException(serviceName, methodType));
        InvocationUtils.invoke(channel, sessionId, msgId, args);
        TransactionTree.TransactionInfo info = txTree.getInfo(methodType)
                .orElseThrow(() -> new UnknownServiceMethodException(serviceName, methodType));
        if (!info.getTree().isCycle()) {
            if (info.getTree().isEmpty()) {
                logger.info("Last message received");
                onCompleted();
            } else {
                txTree = info.getTree();
            }
        }
    }

    public void onCompleted() {
        isDone.set(true);
    }

    private void checkIsDone() {
        if (isDone.get()) {
            throw new ServiceException(serviceName, "Session is completed.");
        }
    }

}
