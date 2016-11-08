package cocaine.netty;

import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.List;

import cocaine.hpack.Decoder;
import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
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
    private final MessageTemplate template;

    public MessageDecoder(MessagePack pack) {
        this.pack = pack;
        this.template = new MessageTemplate();

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        logger.debug("Decoding message");
        in.markReaderIndex();

        ByteBuffer buffer = in.nioBuffer();
        try {
            Message message = pack.read(buffer, template);
            logger.debug("Message has been successfully decoded: " + message);
            in.readerIndex(in.readerIndex() + buffer.position());
            out.add(message);
        } catch (EOFException e) {
            logger.debug("Not enough bytes. Reader index has been reset");
            in.resetReaderIndex();
        }
    }

}
