package io.m18.jmemcached.protocol;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Daum
 */
public enum Op {

    GET, GETS, APPEND, PREPEND, DELETE, DECR,
    INCR, REPLACE, ADD, SET, CAS, STATS, VERSION,
    QUIT, FLUSH_ALL, VERBOSITY, TOUCH;

    private static Map<ChannelBuffer, Op> opsbf = new HashMap<ChannelBuffer, Op>();

    static {
        for (int x = 0; x < Op.values().length; x++) {
            byte[] bytes = Op.values()[x].toString().toLowerCase().getBytes();
            opsbf.put(ChannelBuffers.wrappedBuffer(bytes), Op.values()[x]);
        }
    }

    public static Op FindOp(ChannelBuffer cmd) {
        cmd.readerIndex(0);
        return opsbf.get(cmd);
    }

}