package cocaine.msgpack;

import cocaine.ServiceInfo;
import cocaine.ServiceInfoV12;
import cocaine.api.ServiceApiV12;
import cocaine.api.ServiceApiV12.TransactionDescription;
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
public class ServiceInfoV12Template extends AbstractTemplate<ServiceInfoV12> {
    private final String name;

    private ServiceInfoV12Template(String name) {
        this.name = name;
    }

    public static Template<ServiceInfoV12> create(String name) {
        return new ServiceInfoV12Template(name);
    }

    @Override
    public void write(Packer packer, ServiceInfoV12 service, boolean required) throws IOException {
        throw new UnsupportedOperationException(ServiceInfo.class.getSimpleName()
                + " can not be encoded by " + ServiceInfoTemplate.class.getSimpleName());
    }

    @Override
    public ServiceInfoV12 read(Unpacker unpacker, ServiceInfoV12 service, boolean required) throws IOException {
        unpacker.readArrayBegin();
        List<SocketAddress> endpoints = unpacker.read(Templates.tList(SocketAddressTemplate.getInstance()));
        unpacker.readInt(); // ignore version
        Map<Integer, TransactionDescription> api = unpacker.read(
                Templates.tMap(Templates.TInteger, TransactionDescriptionTemplate.getInstance()));
        unpacker.readArrayEnd();

        return new ServiceInfoV12(name, endpoints, new ServiceApiV12(api));
    }
}
