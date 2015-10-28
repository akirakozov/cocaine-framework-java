package cocaine;

import cocaine.api.ServiceApi;

import java.net.SocketAddress;
import java.util.List;

/**
 * @author akirakozov
 */
public class ServiceInfo {
    private final String name;
    private final List<SocketAddress> endpoints;
    private final ServiceApi api;

    public ServiceInfo(String name, List<SocketAddress> endpoints, ServiceApi api) {
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

    public ServiceApi getApi() {
        return api;
    }

}
