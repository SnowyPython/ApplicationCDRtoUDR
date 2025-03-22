package ru.vatolin.applicationcdrtoudr.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.bind.annotation.*;
import ru.vatolin.applicationcdrtoudr.repository.UDR;
import ru.vatolin.applicationcdrtoudr.service.UDRGeneratorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Класс REST контроллера отвечающего за обработку запросов для работы с udr
 *
 * <p>Основные методы:
 * <ul>
 *   <li>{@link #generateUDReport(String, String, String)} — генерирует UDR отчет для конкретного абонента за определенный период в JSON формате.</li>
 *   <li>{@link #generateUDReportForEveryone(String)} — генерирует UDR отчет для всех абонентов за определенный месяц в JSON формате.</li>
 * </ul>
 *
 * <p>Вспомогательные методы:
 * <ul>
 *   <li>{@link #convertNumberOfMonth(String)} — отвечает за конвертацию и валидацию номера месяца из строки в число.</li>
 * </ul>
 *
 * <p>Класс взаимодействует с:
 * <ul>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.service.UDRGeneratorService} — сервис отвечающий за генерацию UDR.</li>
 * </ul>
 *
 * <p>Для работы используются:
 * <ul>
 *   <li>{@link java.util.ArrayList} — для работы с коллекциями.</li>
 *   <li>{@link java.util.List} — для работы с коллекциями.</li>
 *   <li>{@link java.util.Map} — для формирования ответов в формате JSON.</li>
 * </ul>
 */
@RestController
@RequestMapping("/udr")
public class UDRController {
    private final UDRGeneratorService udrGeneratorService;

    public UDRController(UDRGeneratorService udrGeneratorService) {
        this.udrGeneratorService = udrGeneratorService;
    }

    /**
     * REST метод принимает GET запросы, обрабатывает данные и передает их для генерации UDR отчета для конкретного абонента
     *
     * @param msisdn номер абонента (@PathVariable)
     * @param period период "M" - месяц или "Y" - год (@RequestParam)
     * @param numberOfMonth номер месяца (@RequestParam)
     * @return ResponseEntity в теле JSON
     */
    @GetMapping("/report/{msisdn}")
    public ResponseEntity<?> generateUDReport(@PathVariable String msisdn,
                                           @RequestParam String period,
                                           @RequestParam(required = false) String numberOfMonth) {

        UDR UDReport;

        //определяем период
        if(period.equals("M")) {
            //номер месяца
            int number;
            try {
                number = convertNumberOfMonth(numberOfMonth);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid month number"));
            }
            try {
                UDReport = udrGeneratorService.generateUDReportForMonth(msisdn, number);
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e));
            }
        } else if(period.equals("Y")) {
            UDReport = udrGeneratorService.generateUDReportForYear(msisdn);
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid period parameter"));
        }
        return ResponseEntity.ok(UDReport);
    }

    /**
     * REST метод принимает GET запросы, обрабатывает данные и передает их для генерации UDR отчета для всех абонентов
     *
     * @param numberOfMonth номер месяца, за который хотим получить отчет (@RequestParam)
     * @return ResponseEntity в теле JSON
     */
    @GetMapping("/report/all")
    public ResponseEntity<?> generateUDReportForEveryone(@RequestParam String numberOfMonth) {
        List<UDR> UDReports = new ArrayList<>();
        List<String> msisdnList = udrGeneratorService.generateMsisdnList();

        int number;
        try {
            number = convertNumberOfMonth(numberOfMonth);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid month number"));
        }

        //генерируем отчеты для каждого абонента
        for (String msisdn : msisdnList) {
            try {
                UDReports.add(udrGeneratorService.generateUDReportForMonth(msisdn, number));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        if(UDReports.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No records for this month"));
        }

        return ResponseEntity.ok(UDReports);
    }

    /**
     * Конвертирует номер месяца из строки в число, также проводит валидацию
     *
     * @param numberOfMonth номер месяца (String)
     * @return номер месяца (int)
     * @throws RuntimeException выбрасывается, если переданная строка не является номером месяца от [1 до 12]
     */
    private int convertNumberOfMonth(String numberOfMonth) {
        int number;
        try {
            number = Integer.parseInt(numberOfMonth);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        if (number < 1 || number > 12) {
            throw new RuntimeException("Invalid month number. Must be between 1 and 12");
        }

        return number;
    }
}
