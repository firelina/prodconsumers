package ru.pln.prodconsumers.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.pln.prodconsumers.model.Agent;

import java.util.Random;
import java.util.concurrent.*;

@Getter
public class QueueService {
    private BlockingQueue<Agent> blockingQueue = new LinkedBlockingQueue<>();
    private ExecutorService executorService;
    private String title;

    public QueueService(Integer countThreads, String title) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(title + "-%d")
                .setDaemon(true)
                .build();
        this.executorService = Executors.newFixedThreadPool(countThreads, threadFactory);
        this.title = title;
    }
}
