package cocaine.http;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class HttpMetaData {
    private static final Logger logger = Logger.getLogger(HttpMetaData.class);

    private final String path;
    private final String queryString;
    private final Map<String, String[]> parameters;

    public HttpMetaData(String pathAndQueryString) {
        final int question = pathAndQueryString.indexOf('?');

        this.path = question == -1 ? pathAndQueryString : pathAndQueryString.substring(0, question);
        this.queryString = question == -1 ? "" : pathAndQueryString.substring(question + 1);
        this.parameters = parse(this.queryString);
    }

    private Map<String, String[]> parse(String queryString) {
        try {
            Multimap<String, String> params = ArrayListMultimap.create();
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = idx == -1 ? pair : URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                String value = idx == -1 ? "" : URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                params.put(key, value);
            }

            Map<String, String[]> paramsMap = new HashMap<>();
            for (String key : params.keySet()) {
                paramsMap.put(key, params.get(key).toArray(new String[0]));
            }
            return paramsMap;
        } catch (UnsupportedEncodingException e) {
            logger.warn("Couldn't parse query string: " + queryString, e);
            throw Throwables.propagate(e);
        }
    }

    public String getPath() {
        return path;
    }

    public String getQueryString() {
        return queryString;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }
}
