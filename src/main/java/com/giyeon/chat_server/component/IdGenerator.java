package com.giyeon.chat_server.component;

import com.giyeon.chat_server.util.Snowflake;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    private Snowflake snowflake = new Snowflake(1,1704067200000L, new TimeComponent());

    public Long nextId() {
        return snowflake.nextId();
    }
}
