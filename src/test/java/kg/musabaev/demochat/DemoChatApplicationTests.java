package kg.musabaev.demochat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoChatApplicationTests {

    @LocalServerPort
    private int port;
    private static final Logger log = Logger.getLogger(DemoChatApplicationTests.class.getName());

    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        this.stompClient = new WebSocketStompClient(
                new SockJsClient(
                        List.of(
                                new WebSocketTransport(
                                        new StandardWebSocketClient()))));
        this.stompClient.start();
    }

    @Test
    void test1() throws Exception {

        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        stompClient.setMessageConverter(new StringMessageConverter());

        StompSession session = stompClient
                .connect(getWsUrl(), new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((String) payload);
            }
        });

        session.send("/app/welcome", "Hello Mike");

        assertEquals("Hello Mike test", blockingQueue.poll(1, TimeUnit.SECONDS));
    }

    @Test
    void test2() throws Exception {

        CompletableFuture<OutputMessage> completableFuture = new CompletableFuture<>();
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession session = stompClient
                .connectAsync(getWsUrl(), new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        session.subscribe("/topic/on-message", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return OutputMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                completableFuture.complete(((OutputMessage) payload));
            }
        });
        session.send("/app/messages", new InputMessage("Hello Mike", "eld"));
        assertEquals("Hello Mike by eld", completableFuture.get(1, TimeUnit.SECONDS).getContent());

    }

    private String getWsUrl() {
        return String.format("ws://localhost:%d/ws", port);
    }
}
