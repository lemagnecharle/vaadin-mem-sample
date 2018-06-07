/**
 * Licensed under Apache License v2.0
 * Original Author: Bob Lee
 */
package org.test;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Ein {@link ObjectOutputStream} der die zu serialisierenden Objekte als Baum
 * darstellt. Erlaubt es, einfach herauszufinden, welche Objekte in der Session
 * nicht serialisierbar sind.
 *
 * Basiert auf einem Blog-Eintrag von &quot;Bob Lee&quot;:
 *
 * <a href="http://blog.crazybob.org/2007/02/debugging-serialization.html">http://blog.crazybob.org/2007/02/debugging-serialization.html</a>
 *
 * Der Standard-OutputStream in {@link ForceSerializationFilter#serialize}
 * kann durch diesen ersetzt werden, damit die Logeintraege uebersichtlicher werden.
 * Achtung: Logeintraege werden um ein vielfaches groesser.
 */
public class DebuggingObjectOutputStream extends ObjectOutputStream {

    private static final Field DEPTH_FIELD;

    static {
        try {
            DEPTH_FIELD = ObjectOutputStream.class
                    .getDeclaredField("depth");
            DEPTH_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    private final List<Object> stack
            = new ArrayList<Object>();

    /**
     * Indicates whether or not OOS has tried to
     * write an IOException (presumably as the
     * result of a serialization error) to the
     * stream.
     */
    private boolean broken = false;

    public DebuggingObjectOutputStream(
            OutputStream out) throws IOException {
        super(out);
        enableReplaceObject(true);
    }

    /**
     * Abuse {@code replaceObject()} as a hook to
     * maintain our stack.
     */
    @Override
    protected Object replaceObject(Object o) {
        // ObjectOutputStream writes serialization
        // exceptions to the stream. Ignore
        // everything after that so we don't lose
        // the path to a non-serializable object. So
        // long as the user doesn't write an
        // IOException as the root object, we're OK.
        int currentDepth = currentDepth();
        if (o instanceof IOException
                && currentDepth == 0) {
            broken = true;
        }
        if (!broken) {
            truncate(currentDepth);
            stack.add(o);
        }
        return o;
    }

    private void truncate(int depth) {
        while (stack.size() > depth) {
            pop();
        }
    }

    private Object pop() {
        return stack.remove(stack.size() - 1);
    }

    /**
     * Returns a 0-based depth within the object
     * graph of the current object being
     * serialized.
     */
    private int currentDepth() {
        try {
            Integer oneBased = (Integer) DEPTH_FIELD.get(this);
            return oneBased - 1;
        }
        catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the path to the last object
     * serialized. If an exception occurred, this
     * should be the path to the non-serializable
     * object.
     */
    public List<Object> getStack() {
        return stack;
    }
}
