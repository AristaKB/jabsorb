package org.jabsorb.serializer;

import java.util.Deque;
import java.util.Stack;
import java.util.ArrayDeque;

/**
 * Context that holds the marshalling mode for a given thread.
 * Based on the mode, different behavior of marshall/unmarshall can be controlled.
 */
public class MarshallingModeContext {

    // Main context (inheritable across child threads)
    private static final InheritableThreadLocal<Deque<MarshallingMode>> current = new InheritableThreadLocal<Deque<MarshallingMode>>() {
        @Override
        protected Deque<MarshallingMode> initialValue() {
            return new ArrayDeque<>();
        }
    };

    /**
     * Pushes the value on to the stack
     * @param mode
     */
    public static void push(MarshallingMode mode) {
        current.get().push(mode);
    }

    /**
     * peeks the current MarshallingMode value from the stack
     * @return
     */
    public static MarshallingMode get() {
        Deque<MarshallingMode> stack = current.get();
        return stack.isEmpty() ? null : stack.peek();
    }

    /**
     * pops out the value from the stack
     */
    public static void pop() {
        Deque<MarshallingMode> stack = current.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }
}
