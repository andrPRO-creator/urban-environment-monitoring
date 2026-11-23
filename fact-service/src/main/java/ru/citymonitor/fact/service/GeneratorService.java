package ru.citymonitor.fact.service;

import org.springframework.beans.factory.annotation.Value;import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;import org.springframework.stereotype.Service;
import ru.citymonitor.fact.dto.FactDtos.BatchResult;import java.time.Duration;import java.util.concurrent.ScheduledFuture;import java.util.concurrent.ThreadLocalRandom;
import jakarta.annotation.PostConstruct;
@Service
public class GeneratorService {
 private final FactWriter writer;private final SynchronizationNotifier notifier;private final ThreadPoolTaskScheduler scheduler;private final long min,max;private final int size;private volatile boolean running;private ScheduledFuture<?> future;
 public GeneratorService(FactWriter writer,SynchronizationNotifier notifier,ThreadPoolTaskScheduler scheduler,@Value("${generator.min-interval-ms}") long min,@Value("${generator.max-interval-ms}") long max,@Value("${generator.batch-size}") int size,@Value("${generator.enabled-on-startup:false}") boolean enabled){this.writer=writer;this.notifier=notifier;this.scheduler=scheduler;this.min=min;this.max=max;this.size=size;if(min<1||max<min||size<1)throw new IllegalArgumentException("Проверьте интервал и размер пачки генератора");this.enabled=enabled;}
 private final boolean enabled;
 @PostConstruct public void initialize(){if(enabled)start();}
 public synchronized String start(){if(running)return "Генератор уже запущен";running=true;scheduleNext();return "Генератор запущен";}
 public synchronized String stop(){running=false;if(future!=null)future.cancel(false);return "Генератор остановлен";}
 public BatchResult runOnce() {
     // После выхода из транзакционного метода журнал уже доступен Graph Service.
     BatchResult result = writer.runBatch(size);
     notifier.notify(result.throughSequence());
     return result;
 }
 private synchronized void scheduleNext(){if(!running)return;long delay=ThreadLocalRandom.current().nextLong(min,max+1);future=scheduler.schedule(()->{try{runOnce();}finally{scheduleNext();}},java.time.Instant.now().plusMillis(delay));}
}
