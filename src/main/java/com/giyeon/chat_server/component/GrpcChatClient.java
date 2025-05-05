package com.giyeon.chat_server.component;

import com.giyeon.chat_server.CommunicationServiceGrpc;
import com.giyeon.chat_server.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class GrpcChatClient {

     private final GrpcChannelFactory channelFactory;


     public boolean send(String text, String targetIp, int targetPort, Long userId) {

         // 1) 직접 ManagedChannel 생성 (캐싱 없이, 최신 targetIp/port 반영)
         ManagedChannel channel = ManagedChannelBuilder
                 .forAddress(targetIp, targetPort)
                 .usePlaintext()
                 .build();

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
         }finally {
             channel.shutdown();
             try {
                 if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                     channel.shutdownNow();
                 }
             } catch (InterruptedException ie) {
                 channel.shutdownNow();
                 Thread.currentThread().interrupt();
             }
         }
     }

}
