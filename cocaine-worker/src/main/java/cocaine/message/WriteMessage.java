package cocaine.message;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class WriteMessage extends WorkerMessage {

    private final byte[] data;

    public WriteMessage(long session, byte[] data, List<List<Object>> headers) {
        super(MessageType.WRITE, session, headers);
        Preconditions.checkNotNull(data, "CHunk data can not be null");

        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "WriteMessage/" + getSession() + ": data size - " + data.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        WriteMessage that = (WriteMessage) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

}
