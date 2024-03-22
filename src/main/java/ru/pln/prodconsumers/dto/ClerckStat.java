package ru.pln.prodconsumers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClerckStat {
    private Integer countClients;
    private String title;
    private Boolean isOccupied;

    public ClerckStat(Integer countClients, String title) {
        this.countClients = countClients;
        this.title = title;
    }
}
