package cocaine.http;

import cocaine.EventHandler;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;

import javax.servlet.http.HttpServlet;
import java.io.IOException;

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
            handleError(resp, e);
        } finally {
            if (resp != null) {
                resp.closeOutput();
            }
        }
    }

    protected void handleError(HttpCocaineResponse resp, Exception e) throws IOException {
        logger.warn("Couldn't process servlet: " + e.getMessage(), e);

        resp.setStatus(HttpStatus.SC_500_INTERNAL_SERVER_ERROR);
        resp.getOutputStream().write((e.getMessage() + "\n").getBytes());
        resp.getOutputStream().write(ExceptionUtils.getFullStackTrace(e).getBytes());
    }
}
