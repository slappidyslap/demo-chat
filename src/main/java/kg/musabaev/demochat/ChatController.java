package kg.musabaev.demochat;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(originPatterns = "*")
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry simpUserRegistry;

    private final List<String> sessions = new ArrayList<>();
    @GetMapping("all")
    @ResponseBody
    Set<SimpUser> allSession() {
        return simpUserRegistry.getUsers();
    }

    @MessageMapping("/welcome")
    void processMessage(
            @Payload String message
    ) {
        messagingTemplate.convertAndSend("/topic/chat", message + " test");
    }

    @SubscribeMapping("/topic/chat")
    void consumeMessage(
            @Payload String message
    ) {
        log.warn("message with body {}", message);
    }


    @MessageMapping("/messages")
    void processMessageForUser(
            Message<InputMessage> inputMessage
    ) {
        InputMessage payload = inputMessage.getPayload();
        Message<OutputMessage> outputMessage
                = MessageBuilder.withPayload(
                        new OutputMessage(payload.getContent() + " by " + payload.getFrom())).build();
        messagingTemplate.convertAndSend(
                "/topic/on-message", outputMessage);
    }

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        log.info("Клиент подключился");
        log.info(event);
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        log.info("Клиент отключился");
        log.info(event);
    }

    @EventListener
    public void  onSubscribe(SessionSubscribeEvent event) {
        log.info("Клиент подписался");
        log.info(event);
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        log.info("Клиент отписался");
        log.info(event);
    }

//    @SubscribeMapping("/topic/chat")
//    void consumeMessageForUser(
//            @Payload String message
//    ) {
//        log.warn("message with body {}", message);
//    }



}
