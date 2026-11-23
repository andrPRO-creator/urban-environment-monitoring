package ru.citymonitor.fact.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.citymonitor.fact.dto.FactDtos.BatchResult;
import ru.citymonitor.fact.entity.*;
import ru.citymonitor.fact.repository.*;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class FactWriter {
    private final FactRepository facts; private final FactChangeRepository changes; private final ObjectRepository objects; private final Random random=new Random();
    public FactWriter(FactRepository facts,FactChangeRepository changes,ObjectRepository objects){this.facts=facts;this.changes=changes;this.objects=objects;}
    @Transactional
    public BatchResult runBatch(int size){
        List<MonitoredObject> allObjects=objects.findAll(); List<Fact> active=new ArrayList<>(facts.findAllByDeletedAtIsNull());
        int added=0,deleted=0; OffsetDateTime now=OffsetDateTime.now();
        boolean shouldDelete=!active.isEmpty() && (active.size()>=size*2 || random.nextBoolean());
        if(shouldDelete){Collections.shuffle(active,random);int count=Math.min(Math.max(1,size/2),active.size());for(Fact f:active.subList(0,count)){f.delete(now);facts.save(f);changes.saveAndFlush(new FactChange(f,"DELETED",now));deleted++;}}
        else {for(int i=0;i<size;i++){MonitoredObject o=allObjects.get(random.nextInt(allObjects.size()));OffsetDateTime occurred=now.minusMinutes(random.nextInt(240));EventType type=EventType.values()[random.nextInt(EventType.values().length)];Severity severity=Severity.values()[random.nextInt(Severity.values().length)];Fact f=new Fact(UUID.randomUUID(),o.getObjectId(),occurred,type.name(),severity.name(),description(type.name(),o.getName()),type==EventType.SENSOR_FAILURE?"sensor-"+(1+random.nextInt(5)):null,o.getLatitude(),o.getLongitude(),now);facts.saveAndFlush(f);changes.saveAndFlush(new FactChange(f,"ADDED",now));added++;}}
        long through=changes.findTopByOrderBySequenceIdDesc().map(FactChange::getSequenceId).orElse(0L);
        return new BatchResult(added,deleted,through);
    }
    private String description(String type,String name){return switch(type){case "TRAFFIC_CONGESTION"->"Замедление движения: "+name;case "ROADWORK_STARTED"->"Начались дорожные работы: "+name;case "ROADWORK_ENDED"->"Работы завершены: "+name;case "AIR_QUALITY_ALERT"->"Повышенный индекс качества воздуха: "+name;case "SENSOR_FAILURE"->"Нет телеметрии датчика: "+name;default->"Плановый осмотр сооружения: "+name;};}
}
