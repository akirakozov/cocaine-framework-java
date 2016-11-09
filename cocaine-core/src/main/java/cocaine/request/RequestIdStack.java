package cocaine.request;

import cocaine.hpack.HeaderField;

import java.util.*;

/**
 * @author metal
 */
public class RequestIdStack {
    private static final Random random = new Random();

    public static final ThreadLocal<State> current = new ThreadLocal<>();

    public static void set(State state) {
        current.set(state);
    }

    public static boolean isEmpty() {
        return current.get() == null || current.get().isEmpty();
    }

    public static void push() {
        State currentState = current.get();
        if (currentState != null) {
            State newState = new State(currentState.traceId, random.nextInt(), currentState.spanId);
            newState.previous = current.get();
            current.set(newState);
        }
    }

    public static void pop() {
        State currentState = current.get();
        if (currentState != null) {
            set(currentState.previous);
        }
    }

    public static class State {
        State previous;
        public final int traceId;
        public final int spanId;
        public final int parentId;

        public State(int traceId, int spanId, int parentId) {
            this.traceId = traceId;
            this.spanId = spanId;
            this.parentId = parentId;
        }

        public State(List<HeaderField> headers) {
            int traceId = 0;
            int spanId = 0;
            int parentId = 0;
            int found = 0;

            for (HeaderField h : headers) {
                if (Arrays.equals(h.name, "trace_id".getBytes())) {
                    traceId = HeaderField.toInt(h.value);
                    found++;
                } else if (Arrays.equals(h.name, "span_id".getBytes())) {
                    spanId = HeaderField.toInt(h.value);
                    found++;
                } else if (Arrays.equals(h.name, "parent_id".getBytes())) {
                    parentId = HeaderField.toInt(h.value);
                    found++;
                }
                if (found == 3) {
                    break;
                }
            }
            if (found != 3) {
                traceId = 0;
                spanId = 0;
                parentId = 0;
            }

            this.traceId = traceId;
            this.spanId = spanId;
            this.parentId = parentId;
        }

        public boolean isEmpty() {
            return traceId == 0 && spanId == 0 && parentId == 0;
        }
    }
}
