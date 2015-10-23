package cocaine.netty;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.List;

import cocaine.messagev12.MessageV12;
import cocaine.msgpack.MessageV12Template;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = Logger.getLogger(MessageDecoder.class);

    private final MessagePack pack;

    public MessageDecoder(MessagePack pack) {
        this.pack = pack;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.debug("Decoding message");
        in.markReaderIndex();

        ByteBuffer buffer = in.nioBuffer();
        try {
            MessageV12 message = pack.read(buffer, MessageV12Template.getInstance());
            logger.debug("Message has been successfully decoded: " + message);
            in.readerIndex(in.readerIndex() + buffer.position());
            out.add(message);
        } catch (EOFException e) {
            logger.debug("Not enough bytes. Reader index has been reset");
            in.resetReaderIndex();
        }
    }

}
