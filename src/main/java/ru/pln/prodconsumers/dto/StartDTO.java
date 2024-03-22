package ru.pln.prodconsumers.dto;

import lombok.Data;

import java.util.List;
@Data
public class StartDTO {
    private AgentDTO agent;
    private List<ConsumerDTO> consumers;
}
