package cocaine.messagev12;

import org.msgpack.type.Value;

import java.util.Objects;

/**
 * @author akirakozov
 */
public class MessageV12 {
    private final int type;
    private final long session;
    private final Value payload;

    public MessageV12(int type, long session, Value payload) {
        this.type = type;
        this.session = session;
        this.payload = payload;
    }

    public int getType() {
        return type;
    }

    public long getSession() {
        return session;
    }

    public Value getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageV12 m = (MessageV12) o;
        return session == m.session && type == m.type && Objects.equals(payload, m.getPayload());
    }

    @Override
    public int hashCode() {
        int result = 31 * type + (int) (session ^ (session >>> 32));
        result = 31 * result + payload.hashCode();
        return result;
    }

}
