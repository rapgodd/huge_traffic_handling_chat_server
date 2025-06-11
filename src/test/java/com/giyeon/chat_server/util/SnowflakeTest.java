package com.giyeon.chat_server.util;

import com.giyeon.chat_server.component.TimeComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SnowflakeTest {

    @Mock
    TimeComponent timeComponent = new TimeComponent();

    @Test
    void nextId_only_different_sequence() throws NoSuchMethodException {

        //given
        Snowflake snowflake = new Snowflake(1,1704067200000L, timeComponent);
        BDDMockito
                .when(timeComponent.getSfCurrentTimestamp(1704067200000L))
                .thenReturn(1704024200000L);

        //when
        long id1 = snowflake.nextId();
        long id2 = snowflake.nextId();

        //then
        assertThat(id1).isNotEqualTo(id2);
    }
}