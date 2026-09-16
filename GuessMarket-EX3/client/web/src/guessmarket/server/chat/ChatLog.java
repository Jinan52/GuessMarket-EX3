package guessmarket.server.chat;

import guessmarket.server.dto.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatLog {

    private final List<ChatMessage> messages =
            new ArrayList<>();


    public synchronized void add(
            ChatMessage message) {

        messages.add(message);
    }


    public synchronized List<ChatMessage> snapshot() {

        return new ArrayList<>(messages);
    }
}
