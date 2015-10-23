package cocaine.netty;

import cocaine.messagev12.MessageV12;
import cocaine.session.SessionsV12;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * @author akirakozov
 */
public class ServiceMessageHandlerV12 extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ServiceMessageHandler.class);

    private final SessionsV12 sessions;

    public ServiceMessageHandlerV12(SessionsV12 sessions) {
        this.sessions = sessions;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Handling message: " + msg);

        MessageV12 message = (MessageV12) msg;
        sessions.onEvent(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessions.onCompleted();
    }

}
