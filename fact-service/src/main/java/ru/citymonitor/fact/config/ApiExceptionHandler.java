package ru.citymonitor.fact.config;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.time.OffsetDateTime;import java.util.*;
import org.springframework.web.bind.MethodArgumentNotValidException;import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice public class ApiExceptionHandler {
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<Map<String,Object>> missing(NoSuchElementException ex){return ResponseEntity.status(404).body(body("NOT_FOUND",ex.getMessage()));}
 @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<Map<String,Object>> bad(IllegalArgumentException ex){return ResponseEntity.badRequest().body(body("INVALID_REQUEST",ex.getMessage()));}
 @ExceptionHandler({MethodArgumentNotValidException.class,MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class}) ResponseEntity<Map<String,Object>> invalid(Exception ex){return ResponseEntity.badRequest().body(body("INVALID_REQUEST","Проверьте параметры запроса и формат JSON"));}
 private Map<String,Object> body(String code,String message){return Map.of("timestamp",OffsetDateTime.now().toString(),"service","fact-service","code",code,"message",message);}
}
