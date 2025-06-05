package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.RoomDetailsDto;
import com.giyeon.chat_server.dto.RoomMessageListDto;
import com.giyeon.chat_server.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.contains;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    public void leaveRoom_test() throws Exception {

        // given
        long roomId = 1L;
        doNothing().when(roomService).leaveRoom(roomId);

        //when & then
        mockMvc.perform(delete("/api/rooms/{roomId}/exit", roomId)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void joinRoom_test() throws Exception {
        //given
        long roomId = 1L;
        doNothing().when(roomService).joinRoom(roomId);

        //when & then
        mockMvc.perform(post("/api/rooms/{roomId}/join", roomId)
                        .with(csrf()))
                        .andExpect(status().isNoContent());
    }

    @Test
    public void closeRoom_test() throws Exception {

        //given
        long roomId = 1L;
        doNothing().when(roomService).closeRoom(roomId);

        //when & then
        mockMvc.perform(put("/api/rooms/{roomId}/close", roomId)
                .with(csrf()))
                .andExpect(status().isNoContent());

    }

    @Test
    public void getRoomDetails_test() throws Exception {
        // given
        long roomId = 1L;
        RoomDetailsDto dto = RoomDetailsDto.builder()
                .roomName("giyeonRoom")
                .roomImageUrl(List.of("image1", "image2"))
                .roomId(1L)
                .joinedUserCount(2)
                .build();
        given(roomService.getRoomDetails(roomId)).willReturn(dto);

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}", roomId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.roomName").value(dto.getRoomName()))
            .andExpect(jsonPath("$.data.roomImageUrl",contains("image1","image2")))
            .andExpect(jsonPath("$.data.joinedUserCount").value(2));
    }

    @Test
    public void getRoomMessages_test() throws Exception {
        // given
        long roomId = 1L;
        RoomMessageListDto msg1 = RoomMessageListDto.builder()
                .message("Hello")
                .createdAt(ZonedDateTime.parse("2023-01-01T10:00:00Z"))
                .isMe(true)
                .unreadCount(1)
                .senderName("giyeon")
                .senderImageUrl("imageUrl1")
                .build();
        RoomMessageListDto msg2 = RoomMessageListDto.builder()
                .message("World")
                .createdAt(ZonedDateTime.parse("2023-01-01T11:00:00Z"))
                .isMe(false)
                .unreadCount(0)
                .senderName("Bob")
                .senderImageUrl("imageUrl2")
                .build();
        List<RoomMessageListDto> messages = List.of(msg1, msg2);
        given(roomService.getRoomMessages(eq(roomId), any(Pageable.class))).willReturn(messages);

        // when & then
        mockMvc.perform(get("/api/rooms/{roomId}/messages", roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].message").value("Hello"))
                .andExpect(jsonPath("$.data[0].isMe").value(true))
                .andExpect(jsonPath("$.data[0].unreadCount").value(1))
                .andExpect(jsonPath("$.data[0].senderName").value("giyeon"))
                .andExpect(jsonPath("$.data[0].senderImageUrl").value("imageUrl1"))
                .andExpect(jsonPath("$.data[1].message").value("World"))
                .andExpect(jsonPath("$.data[1].isMe").value(false))
                .andExpect(jsonPath("$.data[1].unreadCount").value(0))
                .andExpect(jsonPath("$.data[1].senderName").value("Bob"))
                .andExpect(jsonPath("$.data[1].senderImageUrl").value("imageUrl2"));
    }
}