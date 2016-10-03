package cocaine;

import rx.Observable;
import rx.Observer;

import java.util.List;

/**
 * @author akirakozov
 */
public interface Invoker {
    void invoke(String event, List<List<Object>> headers, Observable<byte[]> request, Observer<byte[]> response) throws Exception;
}

