package com.giyeon.chat_server.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "grpc.client.chat-server")
@Getter
@ToString
@AllArgsConstructor
public class GrpcProperty {
    private String address;
}
