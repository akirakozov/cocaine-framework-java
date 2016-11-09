package cocaine.netty;

import cocaine.msgpack.InvocationRequestTemplate;
import cocaine.service.InvocationUtils.InvocationRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class InvocationRequestEncoder extends MessageToByteEncoder<InvocationRequest> {
    private static final Logger logger = Logger.getLogger(InvocationRequestEncoder.class);

    private final MessagePack pack;
    private final InvocationRequestTemplate template = new InvocationRequestTemplate();

    public InvocationRequestEncoder(MessagePack pack) {
        super(InvocationRequest.class);
        this.pack = pack;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        ctx.flush();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, InvocationRequest msg, ByteBuf out) throws Exception {
        logger.debug("Encoding message packable: " + msg);
        out.writeBytes(pack.write(msg, template));
    }
}
