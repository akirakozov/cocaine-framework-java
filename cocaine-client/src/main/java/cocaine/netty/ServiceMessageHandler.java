package cocaine.netty;

import cocaine.message.Message;
import cocaine.session.Sessions;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.log4j.Logger;

/**
 * @author akirakozov
 */
public class ServiceMessageHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = Logger.getLogger(ServiceMessageHandler.class);

    private final Sessions sessions;
    private final String serviceName;

    public ServiceMessageHandler(String serviceName, Sessions sessions) {
        this.serviceName = serviceName;
        this.sessions = sessions;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Handling message for service " + serviceName + ": " + msg);

        Message message = (Message) msg;
        sessions.onEvent(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessions.onCompleted();
    }

}
