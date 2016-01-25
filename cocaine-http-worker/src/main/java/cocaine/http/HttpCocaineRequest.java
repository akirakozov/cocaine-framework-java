package cocaine.http;

import cocaine.http.io.HttpCocaineInputStream;
import cocaine.io.CocaineChannelInputStream;
import com.google.common.base.Throwables;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import rx.Observable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * @author akirakozov
 */
public class HttpCocaineRequest implements HttpServletRequest {
    private static final Logger logger = Logger.getLogger(HttpCocaineRequest.class);
    private static final MessagePack PACK = new MessagePack();

    private final HttpInitialRequest request;
    private final HttpCocaineInputStream inputStream;
    private final HttpMetaData metaData;

    public HttpCocaineRequest(Observable<byte[]> request) {
        Iterator<byte[]> it = request.toBlocking().getIterator();
        byte[] initialHttpChunk = it.next();

        this.request = readInitialHttpChunk(initialHttpChunk);
        this.metaData = new HttpMetaData(this.request.getPath());
        this.inputStream = new HttpCocaineInputStream(
                new CocaineChannelInputStream(this.request.getFirstBodyPart(), it));
    }

    private HttpInitialRequest readInitialHttpChunk(byte[] initialHttpChunk) {
        try {
            return PACK.read(initialHttpChunk, HttpInitialRequestTemplate.getInstance());
        } catch (IOException e) {
            logger.warn("Couldn't parse initial http chunk", e);
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDateHeader(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name).orElse(null);
    }

    @Override
    public Enumeration getHeaders(String name) {
        return Collections.enumeration(request.getHeaders(name));
    }

    @Override
    public Enumeration getHeaderNames() {
        return Collections.enumeration(request.getHeaderNames());
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return -1;
        }
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return metaData.getPath();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        return metaData.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        return metaData.getPath();
    }

    @Override
    public StringBuffer getRequestURL() {
        // There are no information about whole url,
        // because of cocaine-http abtract level
        return new StringBuffer(metaData.getPath());
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return getIntHeader(HttpHeader.CONTENT_LENGTH.getValue());
    }

    @Override
    public String getContentType() {
        return getHeader(HttpHeader.CONTENT_TYPE.getValue());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        String[] values = getParameterValues(name);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(metaData.getParameters().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return metaData.getParameters().get(name);
    }

    @Override
    public Map getParameterMap() {
        return metaData.getParameters();
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }
}
