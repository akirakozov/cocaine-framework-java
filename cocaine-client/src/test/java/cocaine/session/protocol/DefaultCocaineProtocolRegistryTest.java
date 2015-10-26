package cocaine.session.protocol;

import cocaine.api.TransactionTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author akirakozov
 */
public class DefaultCocaineProtocolRegistryTest {

    @Test
    public void findProtocol() throws Exception {
        DefaultCocaineProtocolRegistry registry = DefaultCocaineProtocolRegistry.getDefaultRegistry();

        Assert.assertTrue(registry.findProtocol(TransactionTree.SIMPLE_VALUE) instanceof PrimitiveProtocol);
    }
}
