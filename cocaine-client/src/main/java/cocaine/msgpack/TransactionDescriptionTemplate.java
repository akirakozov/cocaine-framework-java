package cocaine.msgpack;

import cocaine.api.ServiceApiV12.TransactionDescription;
import cocaine.api.TransactionTree;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class TransactionDescriptionTemplate extends AbstractTemplate<TransactionDescription> {
    private static final Template<TransactionDescription> instance = new TransactionDescriptionTemplate();

    public static Template<TransactionDescription> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer pk, TransactionDescription v, boolean required) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionDescription read(Unpacker unpacker, TransactionDescription to, boolean required) throws IOException {
        // 1: ['verbosity', {}, {0: ['value', {}], 1: ['error', {}]}]
        unpacker.readArrayBegin();
        String messageName = unpacker.readString();
        TransactionTree txTree = unpacker.read(TransactionTreeTemplate.getInstance());
        TransactionTree rxTree = unpacker.read(TransactionTreeTemplate.getInstance());
        unpacker.readArrayEnd();

        return new TransactionDescription(messageName, rxTree, txTree);
    }
}
