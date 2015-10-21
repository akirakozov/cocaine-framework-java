package cocaine;

import cocaine.api.ServiceApiV12;

import java.net.SocketAddress;
import java.util.List;

/**
 * @author akirakozov
 */
public class ServiceInfoV12 {
    private final String name;
    private final List<SocketAddress> endpoints;
    private final ServiceApiV12 api;

    public ServiceInfoV12(String name, List<SocketAddress> endpoints, ServiceApiV12 api) {
        this.name = name;
        this.endpoints = endpoints;
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public List<SocketAddress> getEndpoints() {
        return endpoints;
    }

    public ServiceApiV12 getApi() {
        return api;
    }

}
