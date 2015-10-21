package cocaine.msgpack;

import cocaine.ServiceInfoV12;
import cocaine.api.ServiceApiV12;
import cocaine.api.TransactionTree;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

/**
 * @author akirakozov
 */
public class ServiceInfoV12TemplateTest {

    @Test
    public void read() throws Exception {
        MessagePack pack = new MessagePack();
        pack.register(SocketAddress.class, SocketAddressTemplate.getInstance());

        // Logger transaction tree
        //{
        //    0: ['emit', {}, {}],
        //    1: ['verbosity', {}, {0: ['value', {}], 1: ['error', {}]}],
        //    2: ['set_verbosity', {}, {0: ['value', {}], 1: ['error', {}]}]
        //}
        Map<Integer, Object> protocol = new HashMap<>();
        protocol.put(0, Arrays.asList("value", Collections.emptyMap()));
        protocol.put(1, Arrays.asList("error", Collections.emptyMap()));

        Map<Integer, Object> tree = new HashMap<>();
        tree.put(0, Arrays.asList("emit", Collections.emptyMap(), Collections.emptyMap()));
        tree.put(1, Arrays.asList("verbosity", Collections.emptyMap(), protocol));
        tree.put(2, Arrays.asList("set_verbosity", Collections.emptyMap(), protocol));

        List<InetSocketAddress> endpoints = Arrays.asList(new InetSocketAddress("localhost", 3456));
        byte[] bytes = pack.write(Arrays.asList(endpoints, 1, tree));
        ServiceInfoV12 info = pack.read(bytes, ServiceInfoV12Template.create("logger"));

        Assert.assertEquals(endpoints, info.getEndpoints());
        Assert.assertEquals("logger", info.getName());

        ServiceApiV12 api = info.getApi();
        Assert.assertEquals(0, api.getMessageId("emit"));
        Assert.assertEquals(1, api.getMessageId("verbosity"));
        Assert.assertEquals(2, api.getMessageId("set_verbosity"));
        Assert.assertTrue(api.getTransmitTree("verbosity").isEmpty());
    }
}
