package ru.pln.prodconsumers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class Agent {
    private String title;
    private Integer state;
    private Long timeGoInQueue = System.currentTimeMillis();


    public Agent(String title, Integer state) {
        this.title = title;
        this.state = state;
    }
}
