package cocaine.message;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import org.msgpack.type.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import cocaine.hpack.HeaderField;

/**
 * @author akirakozov
 */
public class Message {
    private final int type;
    private final long session;
    private final Value payload;
    private List<HeaderField> headers;

    public Message(int type, long session, Value payload, List<HeaderField> headers) {
        this.type = type;
        this.session = session;
        this.payload = payload;
        this.headers = headers;
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

    public List<HeaderField> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message m = (Message) o;
        return session == m.session && type == m.type && Objects.equals(payload, m.getPayload());
    }

    @Override
    public int hashCode() {
        int result = 31 * type + (int) (session ^ (session >>> 32));
        result = 31 * result + payload.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "session " + session + ", type " + type;
     }
}
