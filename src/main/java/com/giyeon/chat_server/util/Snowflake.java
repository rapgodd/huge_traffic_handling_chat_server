package com.giyeon.chat_server.util;

import com.giyeon.chat_server.component.TimeComponent;

public class Snowflake {
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private final long nodeId;
    private final long customEpoch;
    private final TimeComponent timeComponent;

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public Snowflake(long nodeId, long customEpoch, TimeComponent timeComponent) {
        this.nodeId = nodeId;
        this.customEpoch = customEpoch;
        this.timeComponent = timeComponent;
    }


    public synchronized long nextId() {
        long currentTimestamp = timeComponent.getSfCurrentTimestamp(customEpoch);

        if (currentTimestamp == lastTimestamp) {
            sequence = sequence + 1;
        } else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;

        return currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
                | (nodeId << SEQUENCE_BITS)
                | sequence;
    }

}
