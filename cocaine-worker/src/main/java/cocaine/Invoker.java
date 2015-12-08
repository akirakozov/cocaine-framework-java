package cocaine;

import rx.Observable;
import rx.Observer;

/**
 * @author akirakozov
 */
public interface Invoker {
    void invoke(String event, Observable<byte[]> request, Observer<byte[]> response) throws Exception;
}

