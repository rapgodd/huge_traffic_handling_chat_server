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

    public List<MessageJdbcDto> fetchAggregates(Map<Long, Long> lastReadByRoom) {
        if (lastReadByRoom.isEmpty()) {
            return List.of();
        }
        // SQL 문자열 동적 생성
        StringBuilder sql = new StringBuilder()
                .append("SELECT\n")
                .append("  m.room_id                AS roomId,\n")
                .append("  SUM(CASE WHEN m.id > l.last_read_id THEN 1 ELSE 0 END) AS unreadCount,\n")
                .append("  SUBSTRING_INDEX(\n")
                .append("    GROUP_CONCAT(m.message ORDER BY m.created_at DESC), ',', 1\n")
                .append("  )                         AS lastMessage,\n")
                .append("  MAX(m.created_at)        AS lastMessageTime\n")
                .append("FROM message m\n")
                .append("JOIN (\n");

        List<Object> params = new ArrayList<>();
        int cnt = 0;
        for (Map.Entry<Long, Long> entry : lastReadByRoom.entrySet()) {
            if (cnt++ > 0) {
                sql.append("  UNION ALL\n");
            }
            sql.append("  SELECT ? AS room_id, ? AS last_read_id\n");
            params.add(entry.getKey());
            params.add(entry.getValue() == null ? 0L : entry.getValue());

        }
        sql.append(") AS l ON m.room_id = l.room_id\n")
                .append("GROUP BY m.room_id\n")
                .append("ORDER BY m.room_id ASC");



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
