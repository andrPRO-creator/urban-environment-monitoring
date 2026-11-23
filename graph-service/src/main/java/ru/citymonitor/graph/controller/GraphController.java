package ru.citymonitor.graph.controller;
import io.swagger.v3.oas.annotations.Operation;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.web.bind.annotation.*;import ru.citymonitor.graph.dto.GraphDtos.*;import ru.citymonitor.graph.service.GraphQueryService;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/v1") public class GraphController {
 private final GraphQueryService query;public GraphController(GraphQueryService query){this.query=query;}
 @GetMapping("/graph") @Operation(summary="Текущее состояние активного графа") public GraphView current(@RequestParam(required=false)String objectId,@RequestParam(required=false)String edgeType){return query.current(objectId,edgeType);}
 @GetMapping("/graph/objects/{objectId}/chain") @Operation(summary="Цепочка фактов объекта на конец даты UTC") public ChainView chain(@PathVariable String objectId,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate asOf){return query.chain(objectId,asOf);}
 @GetMapping("/graph/changes") @Operation(summary="История структурных изменений графа с пагинацией") public List<ChangeItem> changes(@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)OffsetDateTime from,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)OffsetDateTime to,@RequestParam(required=false)String objectId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){check(page,size);return query.changes(from,to,objectId,page,size);}
 @GetMapping("/facts/{factId}/lifecycle") @Operation(summary="Жизненный цикл факта и история позиции в цепочке") public Lifecycle lifecycle(@PathVariable UUID factId){return query.lifecycle(factId);}
 @GetMapping("/graph/versions") @Operation(summary="Список сохранённых версий графа") public List<VersionSummary> versions(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){check(page,size);return query.versions(page,size);}
 @GetMapping("/graph/versions/{versionId}") @Operation(summary="Снимок выбранной версии графа") public GraphView version(@PathVariable long versionId){return query.version(versionId);}
 private void check(int page,int size){if(page<0||size<1||size>500)throw new IllegalArgumentException("page должен быть >= 0, size — от 1 до 500");}
}
