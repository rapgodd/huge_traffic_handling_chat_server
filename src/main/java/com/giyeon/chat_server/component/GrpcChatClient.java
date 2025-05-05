package com.giyeon.chat_server.component;

import com.giyeon.chat_server.CommunicationServiceGrpc;
import com.giyeon.chat_server.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GrpcChatClient {

    private final ConcurrentHashMap<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();

    private ManagedChannel getOrCreateChannel(String targetIp, int targetPort) {
        String key = targetIp + ":" + targetPort;
        return channelCache.computeIfAbsent(key, k -> ManagedChannelBuilder
                .forAddress(targetIp, targetPort)
                .usePlaintext()
                .build());
    }

    public boolean send(String text, String targetIp, int targetPort, Long userId) {


        ManagedChannel channel = getOrCreateChannel(targetIp, targetPort);

        // 2) Stub 생성
        CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                CommunicationServiceGrpc.newBlockingStub(channel);

        // 3) RPC 호출
        try {
            stub.sendMessage(Message.SendMessageRequest.newBuilder().setUserId(userId)
                    .setContent(text)
                    .build());
            System.out.println("전송 성공, 메세지: " + text);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
