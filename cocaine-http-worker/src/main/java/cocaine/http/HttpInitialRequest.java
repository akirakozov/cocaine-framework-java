package cocaine.http;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * @author akirakozov
 */
public class HttpInitialRequest {
    // TODO: make enam
    private final String method;
    private final String path;
    private final String httpVersion;
    private final Multimap<String, String> headers;
    private final byte[] body;

    public HttpInitialRequest(
            String method, String path, String httpVersion,
            Multimap<String, String> headers, byte[] body)
    {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public byte[] getFirstBodyPart() {
        return body;
    }

    public Multimap<String, String> getHeaders() {
        // TODO: return read-only copy
        return headers;
    }

    public Optional<String> getHeader(String name) {
        return headers.get(name).stream().findFirst();
    }

    public Collection<String> getHeaders(String name) {
        return headers.get(name);
    }

    public Set<String> getHeaderNames() {
        return headers.keySet();
    }
}
