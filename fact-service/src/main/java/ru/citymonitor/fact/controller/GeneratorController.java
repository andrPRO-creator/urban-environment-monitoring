package ru.citymonitor.fact.controller;
import io.swagger.v3.oas.annotations.Operation;import org.springframework.web.bind.annotation.*;import ru.citymonitor.fact.dto.FactDtos.BatchResult;import ru.citymonitor.fact.service.GeneratorService;import java.util.Map;
@RestController @RequestMapping("/api/v1/generator")
public class GeneratorController {
 private final GeneratorService generator;public GeneratorController(GeneratorService generator){this.generator=generator;}
 @PostMapping("/start") @Operation(summary="Запустить генерацию с случайными интервалами") public Map<String,String> start(){return Map.of("status",generator.start());}
 @PostMapping("/stop") @Operation(summary="Остановить генерацию") public Map<String,String> stop(){return Map.of("status",generator.stop());}
 @PostMapping("/run-once") @Operation(summary="Немедленно сформировать один пакет и синхронизировать граф") public BatchResult once(){return generator.runOnce();}
}
