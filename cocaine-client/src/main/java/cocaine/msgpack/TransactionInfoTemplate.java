package cocaine.msgpack;

import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class TransactionInfoTemplate extends AbstractTemplate<TransactionInfo> {
    private static final Template<TransactionInfo> instance = new TransactionInfoTemplate();

    public static Template<TransactionInfo> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer pk, TransactionInfo v, boolean required) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionInfo read(Unpacker unpacker, TransactionInfo to, boolean required) throws IOException {
        unpacker.readArrayBegin();
        String messageName = unpacker.readString();
        TransactionTree tree = unpacker.read(TransactionTreeTemplate.getInstance());
        unpacker.readArrayEnd();

        return new TransactionInfo(messageName, tree);
    }
}
