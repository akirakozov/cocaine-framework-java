package cocaine.http;

import cocaine.EventHandler;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;

import javax.servlet.http.HttpServlet;

/**
 * @author akirakozov
 */
public class HttpEventHandler implements EventHandler {
    private static final Logger logger = Logger.getLogger(HttpEventHandler.class);

    private final HttpServlet servlet;
    
    public HttpEventHandler(HttpServlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void handle(Observable<byte[]> request, Observer<byte[]> response) throws Exception {
        HttpCocaineResponse resp = null;

        try {
            HttpCocaineRequest req = new HttpCocaineRequest(request);
            resp = new HttpCocaineResponse(response);
            servlet.service(req, resp);
        } catch (Exception e) {
            logger.debug("Couldn't process servlet", e);
            throw  e;
        } finally {
            if (resp != null) {
                resp.closeOutput();
            }
        }
    }
}
