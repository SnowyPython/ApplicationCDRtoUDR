package ru.vatolin.applicationcdrtoudr.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vatolin.applicationcdrtoudr.repository.CDR;
import ru.vatolin.applicationcdrtoudr.repository.CDRepository;
import ru.vatolin.applicationcdrtoudr.repository.Subscriber;
import ru.vatolin.applicationcdrtoudr.repository.SubscriberRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CDRGeneratorServiceTest {
    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private CDRepository cdRepository;

    @InjectMocks
    private CDRGeneratorService cdrGeneratorService;

    @TempDir
    Path tempDir;

    /**
     * Проверяем роботоспособность generateCDRecords()
     */
    @Test
    void testGenerateCDRecords() {
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79251256677");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79251258899");

        //задаем поведение Mock объекту
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2));

        //запускаем проверяемый метод
        cdrGeneratorService.generateCDRecords();

        //проверяем, что save сработал и принял объект класса CDR
        verify(cdRepository, atLeastOnce()).save(any(CDR.class));
        //проверяем, что findAll сработал хоть раз
        verify(subscriberRepository, times(1)).findAll();
    }

    /**
     * Проверяем чтобы дата начала звонка была до даты окончания звонка
     */
    @Test
    void testGenerateCDRecords_startTime() {
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79251256677");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79251258899");

        //задаем поведение Mock объекту
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2));

        //запускаем проверяемый метод
        cdrGeneratorService.generateCDRecords();

        //проверяем чтобы save сработал хоть раз, а также корректность началального времени
        verify(cdRepository, atLeastOnce()).save(argThat(cdr -> cdr.getStartTime().isBefore(cdr.getEndTime())));
    }

    /**
     * Проверяем чтобы дата окончания звонка не ушла в будущее
     */
    @Test
    void testGenerateCDRecords_endTime() {
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79251256677");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79251258899");

        //задаем поведение Mock объекту
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2));

        //запускаем проверяемый метод
        cdrGeneratorService.generateCDRecords();

        //проверяем чтобы save сработал хоть раз, а также корректность конечного времени
        verify(cdRepository, atLeastOnce()).save(argThat(cdr -> cdr.getEndTime().isBefore(LocalDateTime.now())));
    }

    /**
     * проверяем чтобы номера в CDR записи не совпадали
     */
    @Test
    void testGenerateCDRecords_numbers() {
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79251256677");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79251258899");

        //задаем поведение Mock объекту
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2));

        //запускаем проверяемый метод
        cdrGeneratorService.generateCDRecords();

        //проверяем чтобы save сработал хоть раз, а также чтобы номер звонящего и принимающего отличались
        verify(cdRepository, atLeastOnce()).save(argThat(cdr -> !cdr.getCallerNumber().equals(cdr.getReceiverNumber())));
    }

    /**
     * проверяем работоспособность метода для генерации CDR отчетов в формате csv
     */
    @Test
    void testGenerateCDReport() {
        //создаем абонентов для тестов
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79251256677");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79251251234");
        Subscriber subscriber3 = new Subscriber();
        subscriber3.setMsisdn("79251251238");
        Subscriber subscriber4 = new Subscriber();
        subscriber4.setMsisdn("79251256699");
        Subscriber subscriber5 = new Subscriber();
        subscriber5.setMsisdn("79251251235");

        //входные данные
        String msisdn = "79251256677";
        LocalDateTime startTime = LocalDateTime.of(2024, 3, 1, 0, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 3, 1, 0, 0, 0);

        //создаем cdr для тестов
        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251238");
        cdr2.setStartTime(LocalDateTime.of(2025, 3, 5, 0, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 3, 5, 2, 0, 0));

        Optional<ArrayList<CDR>> optionalIncoming = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));

        //задаем поведение Mock объектам
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2, subscriber3, subscriber4, subscriber5));
        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(optionalIncoming);

        //запускаем тестируемый метод
        String reportId = cdrGeneratorService.generateCDReport(msisdn, startTime, endTime);

        //путь файла
        Path filePath = Paths.get("src/main/resources/reports", msisdn + "_" + reportId + ".csv");
        //проверяем создается ли файл
        Assertions.assertTrue(Files.exists(filePath), "Файл не был создан");

        try {
            //получаем данные из файла в формате списка строк
            List<String> lines = Files.readAllLines(filePath);

            //проверяем кол-во строк
            Assertions.assertEquals(2, lines.size(), "Файл должен содержать 2 строки (заголовок + строка)");
            //проверяем корректность заголовка
            Assertions.assertTrue(lines.get(0).contains("callType,callerNumber,receiverNumber,startTime,endTime"), "некорректный заголовок");
            //проверяем корректность строки
            Assertions.assertTrue(lines.get(1).contains("01, 79251256677, 79251251234, 2025-01-01T00:00, 2025-01-01T02:00"), "некорректная первая строка");
            //удаляем тестовый отчет
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
