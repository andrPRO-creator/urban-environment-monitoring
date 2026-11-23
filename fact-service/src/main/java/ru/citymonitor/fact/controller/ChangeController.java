package ru.citymonitor.fact.controller;
import io.swagger.v3.oas.annotations.Operation;import io.swagger.v3.oas.annotations.Hidden;import org.springframework.data.domain.PageRequest;import org.springframework.web.bind.annotation.*;import ru.citymonitor.fact.dto.FactDtos.ChangeDto;import ru.citymonitor.fact.repository.FactChangeRepository;import java.util.List;
@RestController @Hidden @RequestMapping("/internal/v1/changes")
public class ChangeController {
 private final FactChangeRepository changes;public ChangeController(FactChangeRepository changes){this.changes=changes;}
 @GetMapping @Operation(summary="Внутреннее чтение журнала синхронизации")
 public List<ChangeDto> changes(@RequestParam(defaultValue="0") long afterSequence,@RequestParam long throughSequence,@RequestParam(defaultValue="500") int limit){if(afterSequence<0||throughSequence<afterSequence||limit<1||limit>1000)throw new IllegalArgumentException("Некорректные параметры курсора/limit");return changes.findBySequenceIdGreaterThanAndSequenceIdLessThanEqualOrderBySequenceIdAsc(afterSequence,throughSequence,PageRequest.of(0,limit)).getContent().stream().map(ChangeDto::from).toList();}
}
