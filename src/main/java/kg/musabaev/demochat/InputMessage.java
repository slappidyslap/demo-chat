package kg.musabaev.demochat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InputMessage {
    private String content;
    private String from;
}
