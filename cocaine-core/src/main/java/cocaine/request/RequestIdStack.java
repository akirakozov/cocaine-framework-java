package cocaine.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author metal
 */
public class RequestIdStack {
    public static final List<Type> AVAILABLE_IDS = Arrays.asList(Type.TRACE_ID, Type.SPAN_ID, Type.PARENT_ID);

    private static final ThreadLocal<Map<Type, String>> currentIds = new ThreadLocal<>();

    public static boolean hasAllIds() {
        return currentIds.get() != null && currentIds.get().size() == AVAILABLE_IDS.size();
    }

    public static String currentId(Type type) {
        return currentIds.get() == null ? null : currentIds.get().get(type);
    }

    public static void pushReplaceId(List<Object> header) {
        if (header.size() == 3) {
            Type type = Type.byHeaderIndex((Integer) header.get(1));
            String id = header.get(2).toString();
            pushReplaceId(type, id);
        }
    }

    public static void pushReplaceId(Type type, String id) {
        if (currentIds.get() == null) {
            currentIds.set(new HashMap<>());
        }
        currentIds.get().put(type, id);

        if (type.equals(Type.TRACE_ID)) {
            currentIds.get().put(Type.PARENT_ID, id);
        }
    }

    public static void popId(Type type) {
        if (currentIds.get() != null) {
            currentIds.get().remove(type);
            if (currentIds.get().isEmpty()) {
                currentIds.remove();
            }
        }
    }

    public static void pop() {
        currentIds.remove();
    }

    public enum Type {
        TRACE_ID(80),
        SPAN_ID(81),
        PARENT_ID(82);

        private final int headerIndex;

        Type(int headerIndex) {
            this.headerIndex = headerIndex;
        }

        public int getHeaderIndex() {
            return headerIndex;
        }

        public static Type byHeaderIndex(int headerIndex) {
            for (Type type : values()) {
                if (type.getHeaderIndex() == headerIndex) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid request id related header index");
        }
    }
}
