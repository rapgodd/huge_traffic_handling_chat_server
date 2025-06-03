package com.giyeon.chat_server.service.msgSender.threadPoolSender;

import com.github.snksoft.crc.CRC;
import com.giyeon.chat_server.component.GrpcChatClient;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class ThreadSendingService {

    private final BlockingQueue<GrpcMessage> messageQueue1;
    private final BlockingQueue<GrpcMessage> messageQueue2;
    private final BlockingQueue<GrpcMessage> messageQueue3;
    private final GrpcChatClient grpcChatClient;
    private final HashMap<Integer,BlockingQueue<GrpcMessage>> messageQueueMap;
    private final ThreadPoolExecutor threadPoolExecutor;

    public ThreadSendingService(GrpcChatClient grpcChatClient,
                                @Qualifier("extractExecutor")ThreadPoolExecutor threadPoolExecutor) {
        this.grpcChatClient = grpcChatClient;
        this.threadPoolExecutor = threadPoolExecutor;
        this.messageQueue1 = new LinkedBlockingQueue<>(5000);
        this.messageQueue2 = new LinkedBlockingQueue<>(5000);
        this.messageQueue3 = new LinkedBlockingQueue<>(5000);
        this.messageQueueMap = new HashMap<>();

        messageQueueMap.put(0, messageQueue1);
        messageQueueMap.put(1, messageQueue2);
        messageQueueMap.put(2, messageQueue3);
    }

    @PostConstruct
    public void init() {
        createThread(messageQueue1);
        createThread(messageQueue2);
        createThread(messageQueue3);
    }

    private void createThread(BlockingQueue<GrpcMessage> messageQueue) {
        threadPoolExecutor.submit(() -> {

            while (true) {
                try {
                    GrpcMessage message = messageQueue.take();
                    String ip = message.ip;
                    int port = message.port;
                    Long userId = message.userId;
                    String json = message.json;

                    grpcChatClient.send(json, ip, port, userId);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });

    }

    public void addMessageToQueue(String json, String ip, int port, Long roomId, Long userId) {
        BlockingQueue<GrpcMessage> messageQueue = determineQueue(roomId);

        messageQueue.add(GrpcMessage.builder()
                .json(json)
                .ip(ip)
                .port(port)
                .userId(userId)
                .build());

    }

    private BlockingQueue<GrpcMessage> determineQueue(Long roomId) {
        int key = (int) CRC.calculateCRC(CRC.Parameters.CCITT, String.valueOf(roomId).getBytes()) & 0x3FFF;
        key = key % 3;
        return messageQueueMap.get(key);
    }


    private static class GrpcMessage{
        private final String json;
        private final String ip;
        private final int port;
        private final Long userId;

        @Builder
        public GrpcMessage(String json, String ip, int port, Long userId) {
            this.json = json;
            this.ip = ip;
            this.port = port;
            this.userId = userId;
        }
    }

    @PreDestroy
    public void clearQueue() throws InterruptedException {

        long deadline = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {
            if (messageQueue1.isEmpty()
                    && messageQueue2.isEmpty()
                    && messageQueue3.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }

    }
}
