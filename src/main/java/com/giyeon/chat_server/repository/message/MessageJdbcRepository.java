package com.giyeon.chat_server.repository.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giyeon.chat_server.dto.MessageJdbcDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MessageJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<MessageJdbcDto> fetchAggregates(Map<Long, LocalDateTime> leavedAtByRoom) {
        // SQL 문자열 동적 생성
        StringBuilder sql = new StringBuilder(
                "SELECT\n" +
                        "  m.room_id             AS roomId,\n" +
                        "  COUNT(*)              AS unreadCount,\n" +
                        "  SUBSTRING_INDEX(\n" +
                        "    GROUP_CONCAT(m.message ORDER BY m.created_at DESC),\n" +
                        "    ',', 1\n" +
                        "  )                      AS lastMessage,\n" +
                        "  MAX(m.created_at)     AS lastMessageTime\n" +
                        "FROM message m\n" +
                        "WHERE "
        );

        List<Object> params = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<Long, LocalDateTime> entry : leavedAtByRoom.entrySet()) {
            if (idx++ > 0) {
                sql.append(" OR ");
            }
            sql.append("(m.room_id = ? AND m.created_at > ?)");
            params.add(entry.getKey());
            params.add(Timestamp.valueOf(entry.getValue()));
        }
        sql.append("\nGROUP BY m.room_id\n");
        sql.append("ORDER BY m.room_id ASC");


         RowMapper<MessageJdbcDto> rowMapper = (rs, rowNum) -> new MessageJdbcDto(
            rs.getLong("roomId"),
            rs.getInt("unreadCount"),
            rs.getString("lastMessage"),
            rs.getTimestamp("lastMessageTime").toLocalDateTime()
         );

        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }



}
