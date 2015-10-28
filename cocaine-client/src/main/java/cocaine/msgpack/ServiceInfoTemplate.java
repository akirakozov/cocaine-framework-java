package cocaine.msgpack;

import cocaine.ServiceInfo;
import cocaine.api.ServiceApi;
import cocaine.api.ServiceApi.TransactionDescription;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author akirakozov
 */
public class ServiceInfoTemplate extends AbstractTemplate<ServiceInfo> {
    private final String name;

    private ServiceInfoTemplate(String name) {
        this.name = name;
    }

    public static Template<ServiceInfo> create(String name) {
        return new ServiceInfoTemplate(name);
    }

    @Override
    public void write(Packer packer, ServiceInfo service, boolean required) throws IOException {
        throw new UnsupportedOperationException(ServiceInfo.class.getSimpleName()
                + " can not be encoded by " + ServiceInfoTemplate.class.getSimpleName());
    }

    @Override
    public ServiceInfo read(Unpacker unpacker, ServiceInfo service, boolean required) throws IOException {
        unpacker.readArrayBegin();
        List<SocketAddress> endpoints = unpacker.read(Templates.tList(SocketAddressTemplate.getInstance()));
        unpacker.readInt(); // ignore version
        Map<Integer, TransactionDescription> api = unpacker.read(
                Templates.tMap(Templates.TInteger, TransactionDescriptionTemplate.getInstance()));
        unpacker.readArrayEnd();

        return new ServiceInfo(name, endpoints, new ServiceApi(name, api));
    }
}
