package ru.pln.prodconsumers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class BankomatStat {
    private Integer countClients;
    private String title;
    private Boolean isOccupied;

    public BankomatStat(Integer countClients, String title) {
        this.countClients = countClients;
        this.title = title;
    }

}
