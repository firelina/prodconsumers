package ru.pln.prodconsumers.service;

import org.springframework.stereotype.Service;
import ru.pln.prodconsumers.dto.StartDTO;
import ru.pln.prodconsumers.dto.StatisticsDTO;

public interface IProdConsumers {
    String start(StartDTO startDTO);
    String stop(String guid);
    StatisticsDTO getStats();
}
