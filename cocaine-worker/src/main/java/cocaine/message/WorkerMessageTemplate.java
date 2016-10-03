package cocaine.message;

import java.io.IOException;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class WorkerMessageTemplate extends AbstractTemplate<WorkerMessage> {

    private static final Template<WorkerMessage> instance = new WorkerMessageTemplate();

    private WorkerMessageTemplate() { }

    public static Template<WorkerMessage> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, WorkerMessage message, boolean required) throws IOException {
        packer.writeArrayBegin(message.getHeaders().isEmpty() ? 3 : 4);
        packer.write(message.getSession());
        packer.write(message.getType().value());

        switch (message.getType()) {
            case HANDSHAKE: {
                HandshakeMessage handshakeMessage = (HandshakeMessage) message;
                packer.writeArrayBegin(1);
                UUIDTemplate.getInstance().write(packer, handshakeMessage.getId());
                packer.writeArrayEnd();
                break;
            }
            case TERMINATE: {
                TerminateMessage terminateMessage = (TerminateMessage) message;
                packer.writeArrayBegin(2);
                packer.write(terminateMessage.getReason().value());
                packer.write(terminateMessage.getMessage());
                packer.writeArrayEnd();
                break;
            }
            case INVOKE: {
                InvokeMessage invokeMessage = (InvokeMessage) message;
                packer.writeArrayBegin(1);
                packer.write(invokeMessage.getEvent());
                packer.writeArrayEnd();
                break;
            }
            case WRITE: {
                WriteMessage chunkMessage = (WriteMessage) message;
                packer.writeArrayBegin(1);
                packer.write(chunkMessage.getData());
                packer.writeArrayEnd();
                break;
            }
            case ERROR: {
                ErrorMessage errorMessage = (ErrorMessage) message;
                packer.writeArrayBegin(2);
                packer.writeArrayBegin(2);
                packer.write(errorMessage.getCategory());
                packer.write(errorMessage.getCode());
                packer.writeArrayEnd();
                packer.write(errorMessage.getMessage());
                packer.writeArrayEnd();
                break;
            }
            case HEARTBEAT:
            case CLOSE: {
                packer.writeArrayBegin(0);
                packer.writeArrayEnd();
                break;
            }
        }

        if (!message.getHeaders().isEmpty()) {
            packer.write(message.getHeaders());
        }

        packer.writeArrayEnd();
    }

    @Override
    public WorkerMessage read(Unpacker unpacker, WorkerMessage message, boolean required) throws IOException {
        throw new UnsupportedOperationException();
    }

}
