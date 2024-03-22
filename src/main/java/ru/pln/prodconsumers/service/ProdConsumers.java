package ru.pln.prodconsumers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pln.prodconsumers.dto.BankomatStat;
import ru.pln.prodconsumers.dto.ClerckStat;
import ru.pln.prodconsumers.dto.StartDTO;
import ru.pln.prodconsumers.dto.StatisticsDTO;
import ru.pln.prodconsumers.exception.MyException;
import ru.pln.prodconsumers.model.Agent;
import ru.pln.prodconsumers.model.Consumer;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


@Component
public class ProdConsumers implements IProdConsumers {
    @Autowired
    private final QueueServiceConfig queueServiceConfig;
    private QueueService commonQueue;
    private StatisticsDTO statisticsDTO = new StatisticsDTO();
    private Lock lock = new ReentrantLock();
    private Lock beforeDelayBancomat = new ReentrantLock();
    private Lock afterDelayBancomat = new ReentrantLock();
    private Lock beforeDelayClerck = new ReentrantLock();
    private Lock afterDelayClerck = new ReentrantLock();
    private QueueService bancomatQueue;
    private QueueService clerckQueue;
    private ConcurrentHashMap<String, Thread> concurrentHashMap = new ConcurrentHashMap<>();
    @Autowired
    public ProdConsumers(QueueServiceConfig queueServiceConfig) {
        this.queueServiceConfig = queueServiceConfig;
    }
//    может работать только один поток в одно время
    @Override
    public String start(StartDTO startDTO) {
        lock.lock();
        if (concurrentHashMap.isEmpty())
        {
            if (Objects.isNull(startDTO)) {
                commonQueue = queueServiceConfig.commonQueue(1, "общая очередь");
                clerckQueue = queueServiceConfig.clerckQueue(4, "клерк");
                bancomatQueue = queueServiceConfig.bancomatQueue(2, "банкомат");
            }
            else {
                commonQueue = queueServiceConfig.commonQueue(1, startDTO.getAgent().getTitle());
                startDTO.getConsumers().forEach(i -> {
                    if(i.getState() == 1){
                        clerckQueue = queueServiceConfig.clerckQueue(i.getCount(), i.getTitle());
                    }
                    if (i.getState() == 2) {
                        bancomatQueue = queueServiceConfig.bancomatQueue(i.getCount(), i.getTitle());
                    }
                });
            }
            Thread thread = new Thread(play());
            String guid = UUID.randomUUID().toString();
            concurrentHashMap.put(guid, thread);
            thread.start();
        }
        lock.unlock();
        return concurrentHashMap.keys().nextElement();
    }

    private Runnable play (){
        return () -> {
            int i = 0;
            while (!Thread.currentThread().isInterrupted() && i < 100) {
                int state = randomBetween(1, 2);
                Agent agent = new Agent("agent" + i, state);
                commonQueue.getExecutorService().execute(commonProducer(agent));
                commonQueue.getExecutorService().submit(commonConsumer);
                i++;
                try {
                    Thread.sleep(100 * randomBetween(5, 15));
                } catch (InterruptedException e) {
                    throw new MyException(e.getMessage());
                }
            }

            concurrentHashMap.clear();
        };
    }
    private Runnable commonProducer(Agent agent){
        return () -> {
            try {
                commonQueue.getBlockingQueue().put(agent);
            } catch (InterruptedException e) {
                throw new MyException(e.getMessage());
            }
        };
    }
    private Runnable commonConsumer = () -> {
        try {
            Agent agent = commonQueue.getBlockingQueue().take();
            if (agent.getState() == 1){
                clerckQueue.getExecutorService().execute(clerckProducer(agent));
                clerckQueue.getExecutorService().submit(clerckConsumer());
            }
            else if (agent.getState() == 2){
                bancomatQueue.getExecutorService().execute(bancomatProducer(agent));
                bancomatQueue.getExecutorService().submit(bancomatConsumer());
            }

        } catch (InterruptedException e) {
            throw new MyException(e.getMessage());
        }
    };
    private Runnable bancomatProducer(Agent agent){
        return () -> {
            try {
                bancomatQueue.getBlockingQueue().put(agent);
            } catch (InterruptedException e) {
                throw new MyException(e.getMessage());
            }
        };
    }
    private Runnable bancomatConsumer(){ return () -> {
        try {
            beforeDelayBancomat.lock();

            Agent consumed = bancomatQueue.getBlockingQueue().take();
            if (System.currentTimeMillis() - consumed.getTimeGoInQueue() / 1000 < 0){
                statisticsDTO.setBankomatNotServied(statisticsDTO.getBankomatNotServied() + 1);
                statisticsDTO.setBankomatQueue(((ThreadPoolExecutor)bancomatQueue.getExecutorService()).getQueue().size());
                return;
            }
            Consumer cons = new Consumer("bancomat");
            cons.consume(consumed);

            final BankomatStat beforeStat = statisticsDTO.getBankomatMap().getOrDefault(Thread.currentThread().getName(), new BankomatStat(0, Thread.currentThread().getName()));
            beforeStat.setIsOccupied(true);
            statisticsDTO.getBankomatMap().put(Thread.currentThread().getName(), beforeStat);
            beforeDelayBancomat.unlock();
            Thread.sleep(100 * randomBetween(10, 30));
            afterDelayBancomat.lock();
            final BankomatStat afterStat = statisticsDTO.getBankomatMap().get(Thread.currentThread().getName());
            afterStat.setCountClients(afterStat.getCountClients() + 1);
            afterStat.setIsOccupied(false);
            statisticsDTO.setBankomatQueue(((ThreadPoolExecutor)bancomatQueue.getExecutorService()).getQueue().size());
            statisticsDTO.getBankomatMap().put(Thread.currentThread().getName(), afterStat);
            afterDelayBancomat.unlock();
        } catch (InterruptedException e) {
            throw new MyException(e.getMessage());
        }
    };
    }
    private Runnable clerckProducer(Agent agent){
        return () -> {
            try {
                clerckQueue.getBlockingQueue().put(agent);
            } catch (InterruptedException e) {
                throw new MyException(e.getMessage());
            }
        };
    }
    private Runnable clerckConsumer(){
        return () -> {
            try {
                beforeDelayClerck.lock();
                Agent consumed = clerckQueue.getBlockingQueue().take();
                if (System.currentTimeMillis() - consumed.getTimeGoInQueue() / 1000 < 0){
                    statisticsDTO.setClerckNotServied(statisticsDTO.getClerckNotServied() + 1);
                    statisticsDTO.setClerckQueue(((ThreadPoolExecutor)clerckQueue.getExecutorService()).getQueue().size());
                    return;
                }
                Consumer cons = new Consumer("clerk");
                cons.consume(consumed);

                final ClerckStat beforeStat = statisticsDTO.getClerckMap().getOrDefault(Thread.currentThread().getName(), new ClerckStat(0, Thread.currentThread().getName()));
                beforeStat.setIsOccupied(true);
                statisticsDTO.getClerckMap().put(Thread.currentThread().getName(), beforeStat);
                beforeDelayClerck.unlock();
                Thread.sleep(100 * randomBetween(30, 50));
                afterDelayClerck.lock();
                final ClerckStat afterStat = statisticsDTO.getClerckMap().get(Thread.currentThread().getName());
                afterStat.setCountClients(afterStat.getCountClients() + 1);
                afterStat.setIsOccupied(false);
                statisticsDTO.setClerckQueue(((ThreadPoolExecutor)clerckQueue.getExecutorService()).getQueue().size());
                statisticsDTO.getClerckMap().put(Thread.currentThread().getName(), afterStat);
                afterDelayClerck.unlock();
            } catch (InterruptedException e) {
                throw new MyException(e.getMessage());
            }
        };
    }

    @Override
    public String stop(String guid) {
        Thread thread = concurrentHashMap.get(guid);
        if (Objects.nonNull(thread)) {
            thread.interrupt();
            commonQueue.getExecutorService().shutdownNow();
            clerckQueue.getExecutorService().shutdownNow();
            bancomatQueue.getExecutorService().shutdownNow();
            commonQueue.getBlockingQueue().clear();
            clerckQueue.getBlockingQueue().clear();
            bancomatQueue.getBlockingQueue().clear();
            concurrentHashMap.remove(guid);
        }
        return guid;
    }

    @Override
    public StatisticsDTO getStats() {
        statisticsDTO.setBankomatList(statisticsDTO.getBankomatMap().values().stream()
                .map(bankomatStat -> new BankomatStat(bankomatStat.getCountClients(), bankomatStat.getTitle(), bankomatStat.getIsOccupied()))
                .sorted((obj1, obj2) -> obj1.getTitle().compareToIgnoreCase(obj2.getTitle()))
                .collect(Collectors.toList()));
        statisticsDTO.setClerckList(statisticsDTO.getClerckMap().values().stream()
                .map(clerckStat -> new ClerckStat(clerckStat.getCountClients(), clerckStat.getTitle(), clerckStat.getIsOccupied()))
                .sorted((obj1, obj2) -> obj1.getTitle().compareToIgnoreCase(obj2.getTitle()))
                .collect(Collectors.toList()));

        return this.statisticsDTO;
    }

    private Integer randomBetween(int minimum, int maximum){
        Random rn = new Random();
        return rn.nextInt(maximum - minimum + 1) + minimum;
    }
}
