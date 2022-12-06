package kg.musabaev.demochat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputMessage implements Serializable {
    private String content;
    private String from;
}
