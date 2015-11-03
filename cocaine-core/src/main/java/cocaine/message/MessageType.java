package cocaine.message;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public enum MessageType {

    HANDSHAKE(0),
    HEARTBEAT(0),
    TERMINATE(1),
    INVOKE(0),
    WRITE(0),
    ERROR(1),
    CLOSE(2),
    ;

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
