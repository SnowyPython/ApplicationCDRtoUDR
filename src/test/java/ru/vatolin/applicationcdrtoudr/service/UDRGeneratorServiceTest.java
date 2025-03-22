package ru.vatolin.applicationcdrtoudr.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vatolin.applicationcdrtoudr.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UDRGeneratorServiceTest {
    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private CDRepository cdRepository;

    @InjectMocks
    private UDRGeneratorService udrGeneratorService;

    /**
     * тестируем генератор Msisdn списка
     */
    @Test
    void testGenerateMsisdnList() {
        //тестовые данные
        Subscriber subscriber1 = new Subscriber();
        subscriber1.setMsisdn("79001002233");
        Subscriber subscriber2 = new Subscriber();
        subscriber2.setMsisdn("79001007788");

        //задаем поведение Mock объекта
        when(subscriberRepository.findAll()).thenReturn(List.of(subscriber1, subscriber2));

        //запускаем тестируемый метод
        List<String> testList = udrGeneratorService.generateMsisdnList();

        //проверяем результат
        Assertions.assertIterableEquals(List.of("79001002233", "79001007788"), testList, "фактический список не совпадает с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за год
     */
    @Test
    void testGenerateUDReportForYear() {
        String msisdn = "79251256677";

        //создаем CDR записи для теста
        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251235");
        cdr2.setStartTime(LocalDateTime.of(2025, 1, 1, 3, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 1, 1, 4, 0, 0));

        CDR cdr3 = new CDR();
        cdr3.setCallType("01");
        cdr3.setCallerNumber("79251251234");
        cdr3.setReceiverNumber("79251256677");
        cdr3.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr3.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr4 = new CDR();
        cdr4.setCallType("02");
        cdr4.setCallerNumber("79251251235");
        cdr4.setReceiverNumber("79251256677");
        cdr4.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr4.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        //получаем Optional объекты
        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>(List.of(cdr3, cdr4)));

        //назначаем поведение Mock объектов
        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        //запускаем тестируемый метод
        UDR udr = udrGeneratorService.generateUDReportForYear(msisdn);

        //проверяем корректность incoming времени
        Assertions.assertEquals("03:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        //проверяем корректность outcoming времени
        Assertions.assertEquals("04:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за год, когда incoming время будет равно 0
     */
    @Test
    void testGenerateUDReportForYear_whenIncomingZero() {
        String msisdn = "79251256677";
        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251251234");
        cdr1.setReceiverNumber("79251256677");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251251235");
        cdr2.setReceiverNumber("79251256677");
        cdr2.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>());
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));

        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        UDR udr = udrGeneratorService.generateUDReportForYear(msisdn);

        Assertions.assertEquals("00:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        Assertions.assertEquals("04:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за год, когда outcoming время будет равно 0
     */
    @Test
    void testGenerateUDReportForYear_whenOutcomingZero() {
        String msisdn = "79251256677";

        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251235");
        cdr2.setStartTime(LocalDateTime.of(2025, 1, 1, 3, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 1, 1, 4, 0, 0));

        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>());

        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        UDR udr = udrGeneratorService.generateUDReportForYear(msisdn);

        Assertions.assertEquals("03:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        Assertions.assertEquals("00:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за месяц
     */
    @Test
    void testGenerateUDReportForMonth() {
        String msisdn = "79251256677";
        //номер месяца
        int number = 1;

        //cdr объекты для тестов
        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251235");
        cdr2.setStartTime(LocalDateTime.of(2025, 2, 1, 3, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 2, 1, 4, 0, 0));

        CDR cdr3 = new CDR();
        cdr3.setCallType("01");
        cdr3.setCallerNumber("79251251234");
        cdr3.setReceiverNumber("79251256677");
        cdr3.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr3.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr4 = new CDR();
        cdr4.setCallType("02");
        cdr4.setCallerNumber("79251251235");
        cdr4.setReceiverNumber("79251256677");
        cdr4.setStartTime(LocalDateTime.of(2025, 2, 1, 0, 0, 0));
        cdr4.setEndTime(LocalDateTime.of(2025, 2, 1, 2, 0, 0));

        //получаем Optional
        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>(List.of(cdr3, cdr4)));

        //задаем поведение Mock объектам
        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        //запускаем тестируемый метод
        UDR udr = udrGeneratorService.generateUDReportForMonth(msisdn, number);

        //проверяем корректность incoming времени
        Assertions.assertEquals("02:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        //проверяем корректность outcoming времени
        Assertions.assertEquals("02:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за месяц когда incoming равно 0
     */
    @Test
    void testGenerateUDReportForMonth_whenIncomingZero() {
        String msisdn = "79251256677";
        int number = 1;

        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251251234");
        cdr1.setReceiverNumber("79251256677");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251251235");
        cdr2.setReceiverNumber("79251256677");
        cdr2.setStartTime(LocalDateTime.of(2025, 2, 1, 0, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 2, 1, 2, 0, 0));

        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>());
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));

        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        UDR udr = udrGeneratorService.generateUDReportForMonth(msisdn, number);

        Assertions.assertEquals("00:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        Assertions.assertEquals("02:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за месяц когда outcoming равно 0
     */
    @Test
    void testGenerateUDReportForMonth_whenOutcomingZero() {
        String msisdn = "79251256677";
        int number = 1;

        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 1, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251235");
        cdr2.setStartTime(LocalDateTime.of(2025, 2, 1, 3, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 2, 1, 4, 0, 0));

        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>());

        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        UDR udr = udrGeneratorService.generateUDReportForMonth(msisdn, number);

        Assertions.assertEquals("02:00:00", udr.getIncomingCall().getTotalTime(), "время в качестве инициатора не сходится с ожидаемым");
        Assertions.assertEquals("00:00:00", udr.getOutcomingCall().getTotalTime(), "время в качестве принимающего не сходится с ожидаемым");
    }

    /**
     * тестируем генерацию UDR отчетов за месяц когда нет записей за заданый месяц
     */
    @Test
    void testGenerateUDReportForMonth_whenNoRecordsForCurrentMonth() {
        String msisdn = "79251256677";
        int number = 1;

        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79251256677");
        cdr1.setReceiverNumber("79251251234");
        cdr1.setStartTime(LocalDateTime.of(2025, 2, 1, 0, 0, 0));
        cdr1.setEndTime(LocalDateTime.of(2025, 2, 1, 2, 0, 0));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79251256677");
        cdr2.setReceiverNumber("79251251235");
        cdr2.setStartTime(LocalDateTime.of(2025, 3, 1, 3, 0, 0));
        cdr2.setEndTime(LocalDateTime.of(2025, 3, 1, 4, 0, 0));

        CDR cdr3 = new CDR();
        cdr3.setCallType("01");
        cdr3.setCallerNumber("79251251234");
        cdr3.setReceiverNumber("79251256677");
        cdr3.setStartTime(LocalDateTime.of(2025, 2, 1, 0, 0, 0));
        cdr3.setEndTime(LocalDateTime.of(2025, 2, 1, 2, 0, 0));

        CDR cdr4 = new CDR();
        cdr4.setCallType("02");
        cdr4.setCallerNumber("79251251235");
        cdr4.setReceiverNumber("79251256677");
        cdr4.setStartTime(LocalDateTime.of(2025, 3, 1, 0, 0, 0));
        cdr4.setEndTime(LocalDateTime.of(2025, 3, 1, 2, 0, 0));

        Optional<ArrayList<CDR>> incomingOptional = Optional.of(new ArrayList<>(List.of(cdr1, cdr2)));
        Optional<ArrayList<CDR>> outcomingOptional = Optional.of(new ArrayList<>(List.of(cdr3, cdr4)));

        when(cdRepository.findIncomingByMsisdn(msisdn)).thenReturn(incomingOptional);
        when(cdRepository.findOutcomingByMsisdn(msisdn)).thenReturn(outcomingOptional);

        //проверяем выбрасывание исключения
        Assertions.assertThrows(RuntimeException.class, () -> udrGeneratorService.generateUDReportForMonth(msisdn, number), "нет ожидаемого исключения");
    }
}
