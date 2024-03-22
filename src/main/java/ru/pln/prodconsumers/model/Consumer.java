package ru.pln.prodconsumers.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@Slf4j
public class Consumer {
    private String title;

    public void consume(Agent agent){
        log.info("title: " + agent.getTitle() + " state: "+ agent.getState());
    }
}
