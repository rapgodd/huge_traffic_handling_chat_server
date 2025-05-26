package com.giyeon.chat_server.component;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class MasterSlaveKeyRouterTest {

    @Test
    public void getSlaveKey(){

        //given
        MasterSlaveKeyRouter masterSlaveKeyRouter = new MasterSlaveKeyRouter();
        masterSlaveKeyRouter.addSlaveKey("1");
        masterSlaveKeyRouter.addSlaveKey("2");
        masterSlaveKeyRouter.addSlaveKey("3");

        //when
        String slaveKey1 = masterSlaveKeyRouter.getSlaveKey();
        String slaveKey2 = masterSlaveKeyRouter.getSlaveKey();
        String slaveKey3 = masterSlaveKeyRouter.getSlaveKey();

        //then
        assertThat(slaveKey1).isNotEqualTo(slaveKey2);
        assertThat(slaveKey2).isNotEqualTo(slaveKey3);
        assertThat(slaveKey1).isNotEqualTo(slaveKey3);
    }

}