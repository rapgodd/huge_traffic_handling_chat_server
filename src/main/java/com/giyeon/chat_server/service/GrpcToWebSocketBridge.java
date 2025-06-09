package com.giyeon.chat_server.service;

import com.giyeon.chat_server.CommunicationServiceGrpc;
import com.giyeon.chat_server.Message;
import com.giyeon.chat_server.exception.customException.NotFoundException;
import com.giyeon.chat_server.exception.customException.SessionSendException;
import com.giyeon.chat_server.service.msgSender.localSender.GrpcWebsocketSender;
import com.giyeon.chat_server.ws.SessionRegistry;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcToWebSocketBridge extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final SessionRegistry sessionRegistry;
    private final GrpcWebsocketSender grpcWebsocketSender;

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
            WebSocketSession webSocketSession = sessionRegistry.getUserSession(userId);
            grpcWebsocketSender.sendToWebSocket(userId,request.getContent(),webSocketSession);

            responseObserver.onNext(
                    Message.SendMessageResponse.newBuilder()
                            .setSuccess(true)
                            .build()
            );
            responseObserver.onCompleted();
        }catch (NotFoundException e) {
            log.error("Failed to find session of userId: {}", userId, e);
            responseObserver.onError(
                    io.grpc.Status.NOT_FOUND
                            .withDescription("WebSocket 전송 실패: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }catch (SessionSendException e) {
            log.error("Failed to send message to userId: {}", userId, e);
            responseObserver.onError(
                    io.grpc.Status.INTERNAL
                            .withDescription("WebSocket 전송 실패: " + e.getMessage())
                            .withCause(e)
                            .asRuntimeException()
            );
        }
    }


}
