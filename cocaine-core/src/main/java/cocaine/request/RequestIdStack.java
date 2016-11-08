package cocaine.request;

import cocaine.hpack.HeaderField;

import java.util.*;

/**
 * @author metal
 */
public class RequestIdStack {
    public static final List<String> AVAILABLE_IDS = Arrays.asList("trace_id", "span_id", "parent_id");
    private static final Random random = new Random();
    public static class State {
        State previous;
        public final int trace_id;
        public final int span_id;
        public final int parent_id;

        public State(int trace_id, int span_id, int parent_id) {
            this.previous = null;
            this.trace_id = trace_id;
            this.span_id = span_id;
            this.parent_id = parent_id;
        }

        public State(List<HeaderField> headers) {
            int trace_id = 0;
            int span_id = 0;
            int parent_id = 0;
            int found = 0;
            for(int i = 0; i < headers.size(); i++) {
                HeaderField h = headers.get(i);
                if(java.util.Arrays.equals(h.name, "trace_id".getBytes())) {
                    trace_id = HeaderField.toInt(h.value);
                    found++;
                } else if(java.util.Arrays.equals(h.name, "span_id".getBytes())) {
                    span_id = HeaderField.toInt(h.value);
                    found++;
                } else if(java.util.Arrays.equals(h.name, "parent_id".getBytes())) {
                    parent_id = HeaderField.toInt(h.value);
                    found++;
                }
                if(found == 3) {
                    break;
                }
            }
            if(found == 3) {
                this.trace_id = trace_id;
                this.span_id = span_id;
                this.parent_id = parent_id;
            } else {
                this.trace_id = 0;
                this.span_id = 0;
                this.parent_id = 0;
            }
        }

        public boolean empty() {
            return trace_id == 0 && span_id == 0 && parent_id == 0;
        }
    }

    public static final ThreadLocal<State> current = new ThreadLocal<State>();

    public static void assign(State state) {
        current.set(state);
    }

    public static boolean empty() {
        return current.get() == null || current.get().empty();
    }

    public static void push() {
        State c = current.get();
        if(c == null) {
            return;
        } else {
            State new_state = new State(c.trace_id, random.nextInt(), c.span_id);
            new_state.previous = current.get();
            current.set(new_state);
        }
    }

    public static void pop() {
        State c = current.get();
        if(c == null || c.previous == null) {
            return;
        } else {
            c = c.previous;
        }
    }
}
