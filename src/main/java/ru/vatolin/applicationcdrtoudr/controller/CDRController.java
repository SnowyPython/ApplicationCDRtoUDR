package ru.vatolin.applicationcdrtoudr.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.vatolin.applicationcdrtoudr.service.CDRGeneratorService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Класс REST контроллера отвечающего за обработку запросов для работы с CDR
 *
 * <p>Основные методы:
 * <ul>
 *   <li>{@link #generateCDR(String, String, String)} — REST метод запускающий генерацию CDR отчета в формате csv.</li>
 * </ul>
 *
 * <p>Вспомогательные методы:
 * <ul>
 *   <li>{@link #convertDate(String)} — отвечает за конвертацию и валидацию даты из строки в LocalDateTime.</li>
 * </ul>
 *
 * <p>Класс взаимодействует с:
 * <ul>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.service.CDRGeneratorService} — сервис отвечающий за генерацию CDR.</li>
 * </ul>
 *
 * <p>Для работы используются:
 * <ul>
 *   <li>{@link java.time.LocalDateTime} — для работы со временем.</li>
 *   <li>{@link java.util.Map} — для формирования ответов в формате JSON.</li>
 * </ul>
 */
@RestController
@RequestMapping("/cdr")
public class CDRController {
    private final CDRGeneratorService cdrGeneratorService;

    public CDRController(CDRGeneratorService cdrGeneratorService) {
        this.cdrGeneratorService = cdrGeneratorService;
    }

    /**
     * REST метод принимающий GET запрос, который запускает генерацию CDR отчета в csv формате, который в дальнейшем помещается в src/main/resources/reports
     *
     * @param msisdn номер абонента для которого генерируем отчет (@PathVariable)
     * @param startDate начало периода, за который будет отчет (@RequestParam)
     * @param endDate конец периода, за который будет отчет (@RequestParam)
     * @return ResponseEntity в теле JSON, который в случае успеха содержит UUID CDR отчета
     */
    @GetMapping("/generate/{msisdn}")
    public ResponseEntity<?> generateCDR(@PathVariable String msisdn, @RequestParam String startDate, @RequestParam String endDate) {
        LocalDateTime startDateTime = convertDate(startDate);
        LocalDateTime endDateTime = convertDate(endDate);
        if (startDateTime == null || endDateTime == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid target date format. Expected format: YYYY-MM-DDTHH:mm:ss"));
        }
        String reportId;
        try {
            reportId = cdrGeneratorService.generateCDReport(msisdn, startDateTime, endDateTime);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "No CDR for " + msisdn));
        }

        return ResponseEntity.ok(Map.of("successfully", reportId));
    }

    /**
     * Конвертирует и валидирует дату из String в LocalDateTime
     *
     * @param targetDate дата для конвертации
     * @return дата в формате LocalDateTime
     */
    private LocalDateTime convertDate(String targetDate) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(targetDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime;
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
