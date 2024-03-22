package ru.pln.prodconsumers.dto;

import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
@Data
public class StatisticsDTO {
    private Integer bankomatQueue;
    private Integer clerckQueue;
    private Integer bankomatNotServied = 0;
    private Integer clerckNotServied = 0;
    private ConcurrentHashMap<String, BankomatStat> bankomatMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ClerckStat> clerckMap = new ConcurrentHashMap<>();
    private List<BankomatStat> bankomatList;
    private List<ClerckStat> clerckList;

}
