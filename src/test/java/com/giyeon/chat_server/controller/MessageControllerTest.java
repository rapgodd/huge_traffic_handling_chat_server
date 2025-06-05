package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.ChatDto;
import com.giyeon.chat_server.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    public void sendMessage_test() throws Exception {
        //given
        doNothing().when(messageService).sendMessage(any(ChatDto.class));

        //when & then
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content("{\"senderId\":1,\"roomId\":2,\"message\":\"Hello\",\"senderName\": \"giyeon\"}")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void send_emptyMessage_test() throws Exception {
        //given
        doNothing().when(messageService).sendMessage(any(ChatDto.class));

        //when & then
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content("{\"senderId\":1,\"roomId\":2,\"message\":\"\",\"senderName\": \"giyeon\"}")
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }
}