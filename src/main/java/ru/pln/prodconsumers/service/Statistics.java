package ru.pln.prodconsumers.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {
    private ConcurrentHashMap<String, Integer> countInQueue;
    private ConcurrentHashMap<String, Boolean> isOcupated;
}
