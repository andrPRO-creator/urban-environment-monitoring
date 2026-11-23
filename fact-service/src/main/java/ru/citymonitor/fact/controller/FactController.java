package ru.citymonitor.fact.controller;

import io.swagger.v3.oas.annotations.Operation;import org.springframework.data.domain.*;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.web.bind.annotation.*;
import ru.citymonitor.fact.dto.FactDtos.*;import ru.citymonitor.fact.entity.Fact;import ru.citymonitor.fact.repository.*;
import java.time.OffsetDateTime;import java.util.*;import java.util.stream.Collectors;

@RestController @RequestMapping("/api/v1")
public class FactController {
 private final FactRepository facts;private final FactChangeRepository changes;private final ObjectRepository objects;
 public FactController(FactRepository facts,FactChangeRepository changes,ObjectRepository objects){this.facts=facts;this.changes=changes;this.objects=objects;}
 @GetMapping("/objects") @Operation(summary="Список объектов синтетического города")
 public List<ObjectDto> objects(){return objects.findAll().stream().map(ObjectDto::from).toList();}
 @GetMapping("/facts") @Operation(summary="Поиск фактов с фильтрами и пагинацией")
 public Page<FactDto> facts(@RequestParam(required=false) String objectId,@RequestParam(required=false) String eventType,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,@RequestParam(defaultValue="false") boolean deleted,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size){
  if(page<0||size<1||size>500)throw new IllegalArgumentException("page должен быть >= 0, size — от 1 до 500");
  if(from!=null&&to!=null&&from.isAfter(to))throw new IllegalArgumentException("from не может быть позже to");
  return facts.search(objectId,eventType,from,to,deleted,PageRequest.of(page,size)).map(FactDto::from);
 }
 @GetMapping("/facts/{factId}") @Operation(summary="Получить факт по UUID")
 public FactDto fact(@PathVariable UUID factId){return facts.findById(factId).map(FactDto::from).orElseThrow(()->new NoSuchElementException("Факт не найден: "+factId));}
}
