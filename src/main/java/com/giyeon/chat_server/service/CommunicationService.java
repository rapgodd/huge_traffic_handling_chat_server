package com.giyeon.chat_server.service;

import com.giyeon.chat_server.CommunicationServiceGrpc;
import com.giyeon.chat_server.Message;
import com.giyeon.chat_server.ws.SessionRegistry;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@GrpcService
@RequiredArgsConstructor
public class CommunicationService extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final SessionRegistry sessionRegistry;

    // 호출 시점
    // 다른 Spring 서버에서 방 안에 있는 유저를
    // 로컬 HashMap에서 찾지 못해 레디스에서 찾기를 시도하고
    // 그 시도가 성공해서 반환 받은 IP로 GRPC 호출을 하면
    // 이 메서드가 호출이 된다.

    // other server --> grpc --> this server --> sendMessage()
    @Override
    public void sendMessage(Message.SendMessageRequest request,
                            StreamObserver<Message.SendMessageResponse> responseObserver) {

        Long userId = request.getUserId();
        try {
            sessionRegistry.getUserSession(userId).sendMessage(new TextMessage(request.getContent()));
        }catch (IOException e) {
            System.out.println("sendMessage IOException: " + e.getMessage());
            responseObserver.onError(e);
            return;
        }

        responseObserver.onNext(
                Message.SendMessageResponse.newBuilder()
                        .setSuccess(true)
                        .build()
        );
        responseObserver.onCompleted();
    }
}
