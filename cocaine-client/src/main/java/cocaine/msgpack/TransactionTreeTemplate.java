package cocaine.msgpack;

import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.Map;

/**
 * @author akirakozov
 */
public class TransactionTreeTemplate extends AbstractTemplate<TransactionTree> {
    private static final Template<TransactionTree> instance = new TransactionTreeTemplate();

    public static Template<TransactionTree> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer pk, TransactionTree v, boolean required) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionTree read(Unpacker unpacker, TransactionTree to, boolean required) throws IOException {
        Map<Integer, TransactionInfo> transactions =
                unpacker.read(Templates.tMap(Templates.TInteger, TransactionInfoTemplate.getInstance()));

        return new TransactionTree(transactions);
    }
}
