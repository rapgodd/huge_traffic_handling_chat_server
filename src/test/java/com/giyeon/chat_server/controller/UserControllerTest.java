package com.giyeon.chat_server.controller;

import com.giyeon.chat_server.dto.RoomCreateDto;
import com.giyeon.chat_server.dto.RoomInfoDto;
import com.giyeon.chat_server.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @MockitoBean
    private RoomService roomService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getMyRooms_test() throws Exception {
        //given
        List<RoomInfoDto> rooms = Arrays.asList(
                RoomInfoDto.builder()
                        .roomName("Room1")
                        .roomImageUrl(Arrays.asList("url1", "url2"))
                        .roomId(1L)
                        .unreadCount(5)
                        .joinedUserCount(10)
                        .lastMessage("Hello Room1")
                        .lastMessageTime(ZonedDateTime.now())
                        .build(),
                RoomInfoDto.builder()
                        .roomName("Room2")
                        .roomImageUrl(Arrays.asList("url3", "url4"))
                        .roomId(2L)
                        .unreadCount(3)
                        .joinedUserCount(8)
                        .lastMessage("Hello Room2")
                        .lastMessageTime(ZonedDateTime.now())
                        .build()
        );
        when(roomService.getUserRooms(any(Pageable.class))).thenReturn(rooms);

        //when & then
        mockMvc.perform(get("/api/me/rooms")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.[0].roomId").value(1))
            .andExpect(jsonPath("$.data.[0].roomName").value("Room1"))
            .andExpect(jsonPath("$.data.[1].roomId").value(2))
            .andExpect(jsonPath("$.data.[1].roomName").value("Room2"));
    }

    @Test
    public void createRoom_test() throws Exception {
        // given
        doNothing().when(roomService).createRoom(any(RoomCreateDto.class));

        // when & then
        mockMvc.perform(post("/api/users/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content("{\"roomName\":\"New Room\",\"roomImageUrl\":\"imageUrl\",\"otherUserIds\":[1,2,3]}")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

}