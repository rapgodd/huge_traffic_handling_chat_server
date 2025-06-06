package com.giyeon.chat_server.component;


import org.springframework.stereotype.Component;

@Component
public class TimeComponent {

    public long getSfCurrentTimestamp(long customEpoch) {
        return System.currentTimeMillis() - customEpoch;
    }

}
