package com.giyeon.chat_server.repository.message;

import com.giyeon.chat_server.dto.MessageJdbcDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MessageJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<MessageJdbcDto> fetchAggregates(Map<Long, ZonedDateTime> leavedAtByRoom) {
        if (leavedAtByRoom.isEmpty()) {
            return List.of();
        }
        // SQL 문자열 동적 생성
        StringBuilder sql = new StringBuilder()
                .append("SELECT\n")
                .append("  m.room_id                AS roomId,\n")
                .append("  SUM(CASE WHEN m.created_at > l.leaveAt THEN 1 ELSE 0 END) AS unreadCount,\n")
                .append("  SUBSTRING_INDEX(\n")
                .append("    GROUP_CONCAT(m.message ORDER BY m.created_at DESC), ',', 1\n")
                .append("  )                         AS lastMessage,\n")
                .append("  MAX(m.created_at)        AS lastMessageTime\n")
                .append("FROM message m\n")
                .append("JOIN (\n");

        int cnt = 0;
        for (Map.Entry<Long, ZonedDateTime> entry : leavedAtByRoom.entrySet()) {
            if (cnt++ > 0) {
                sql.append("  UNION ALL\n");
            }
            sql.append("  SELECT ? AS room_id, ? AS leaveAt\n");
        }
        sql.append(") AS l ON m.room_id = l.room_id\n")
                .append("GROUP BY m.room_id\n")
                .append("ORDER BY m.room_id ASC");


        List<Object> params = new ArrayList<>();
        for (Map.Entry<Long, ZonedDateTime> entry : leavedAtByRoom.entrySet()) {
            params.add(entry.getKey());
            params.add(entry.getValue());  // null 허용. CASE WHEN 절에서 false 처리 → unreadCount=0
        }


        RowMapper<MessageJdbcDto> rowMapper = (rs, rowNum) -> {
            Timestamp ts = rs.getTimestamp("lastMessageTime");
            ZonedDateTime lastTime = ts == null ? null : ts.toInstant().atZone(ZoneId.systemDefault());
            return new MessageJdbcDto(
                    rs.getLong("roomId"),
                    rs.getInt("unreadCount"),
                    rs.getString("lastMessage"),
                    lastTime
            );
        };

        return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
    }



}
