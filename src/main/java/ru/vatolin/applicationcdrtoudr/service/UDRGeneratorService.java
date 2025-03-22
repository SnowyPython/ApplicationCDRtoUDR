package ru.vatolin.applicationcdrtoudr.service;

import org.springframework.stereotype.Service;
import ru.vatolin.applicationcdrtoudr.repository.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Данный класс выступает в качестве сервиса для работы с UDR-отчетами.
 * Он предоставляет методы для генерации отчетов за месяц и год, а также вспомогательные методы
 * для работы с данными.
 *
 * <p>Основные методы:
 * <ul>
 *   <li>{@link #generateUDReportForMonth(String, int)} — генерирует UDR-отчет за указанный месяц.</li>
 *   <li>{@link #generateUDReportForYear(String)} — генерирует UDR-отчет за указанный год.</li>
 * </ul>
 *
 * <p>Вспомогательные методы:
 * <ul>
 *   <li>{@link #generateMsisdnList()} — генерирует список номеров абонентов.</li>
 *   <li>{@link #createIncomingList(String)} — создает список входящих звонков для указанного абонента.</li>
 *   <li>{@link #createOutcomingList(String)} — создает список исходящих звонков для указанного абонента.</li>
 * </ul>
 *
 * <p>Класс взаимодействует с:
 * <ul>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.repository.CDRepository} — репозиторий для работы с CDR-записями.</li>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.repository.SubscriberRepository} — репозиторий для работы с абонентами.</li>
 * </ul>
 *
 * <p>Для работы используются:
 * <ul>
 *   <li>{@link java.time.Duration} — для работы с длительностью звонков.</li>
 *   <li>{@link java.util.ArrayList} — для хранения списков.</li>
 *   <li>{@link java.util.List} — для работы с коллекциями.</li>
 *   <li>{@link java.util.Optional} — для обработки возможных отсутствующих значений.</li>
 * </ul>
 */
@Service
public class UDRGeneratorService {
    private final CDRepository cdRepository;
    private final SubscriberRepository subscriberRepository;

    public UDRGeneratorService(CDRepository cdRepository, SubscriberRepository subscriberRepository) {
        this.cdRepository = cdRepository;
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Данный метод генерирует UDR отчет за месяц для конкретного обонента
     *
     * @param msisdn номер абонента для которого генерируется отчет
     * @param numberOfMonth номер месяца, за который необходимо сгенерировать отчет
     * @return UDR отчет за месяц по заданному пользователю
     * @throws RuntimeException если не найдено записей за этот месяц
     */
    public UDR generateUDReportForMonth(String msisdn, int numberOfMonth) {
        UDR udr = new UDR();

        Duration incomingTime = Duration.ZERO;
        Duration outcomingTime = Duration.ZERO;

        //список CDR записей, где абонент выступал в качесвте инициатора
        List<CDR> incomingList = createIncomingList(msisdn);
        //список CDR записей, где абонент выступал в качесвте принимающего
        List<CDR> outcomingList = createOutcomingList(msisdn);

        //считаем итоговое время в качестве инициатора
        for (CDR cdr : incomingList) {
            //берем только нужный нам месяц
            if (cdr.getStartTime().getMonthValue() == numberOfMonth) {
                Duration durationBetween = Duration.between(cdr.getStartTime(), cdr.getEndTime());
                incomingTime = incomingTime.plus(durationBetween);
            }
        }

        //считаем итоговое время в качестве принимающего
        for (CDR cdr : outcomingList) {
            //берем только нужный нам месяц
            if (cdr.getStartTime().getMonthValue() == numberOfMonth) {
                Duration durationBetween = Duration.between(cdr.getStartTime(), cdr.getEndTime());
                outcomingTime = outcomingTime.plus(durationBetween);
            }
        }

        //выбрасываем исключение, если нет записей за этот месяц
        if (incomingTime.equals(Duration.ZERO) && outcomingTime.equals(Duration.ZERO)) {
            throw new RuntimeException("No record for " + numberOfMonth + " month");
        }

        //передаем значения в объект типа UDR
        udr.setMsisdn(msisdn);
        UDR.CallDetail incomingCall = new UDR.CallDetail();
        incomingCall.setTotalTime(incomingTime);
        udr.setIncomingCall(incomingCall);

        UDR.CallDetail outcomingCall = new UDR.CallDetail();
        outcomingCall.setTotalTime(outcomingTime);
        udr.setOutcomingCall(outcomingCall);

        return udr;
    }

    /**
     * Данный метод генерирует UDR отчет за год для конкретного человека
     *
     * @param msisdn номер абонента для которого генерируется отчет
     * @return UDR отчет за год по заданному пользователю
     */
    public UDR generateUDReportForYear(String msisdn) {
        UDR udr = new UDR();

        Duration incomingTime = Duration.ZERO;
        Duration outcomingTime = Duration.ZERO;

        //список CDR записей, где абонент выступал в качесвте инициатора
        List<CDR> incomingList = createIncomingList(msisdn);
        //список CDR записей, где абонент выступал в качесвте принимающего
        List<CDR> outcomingList = createOutcomingList(msisdn);

        //считаем итоговое время в качестве инициатора
        for (CDR cdr : incomingList) {
            Duration durationBetween = Duration.between(cdr.getStartTime(), cdr.getEndTime());
            incomingTime = incomingTime.plus(durationBetween);
        }

        //считаем итоговое время в качестве принимающего
        for (CDR cdr : outcomingList) {
            Duration durationBetween = Duration.between(cdr.getStartTime(), cdr.getEndTime());
            outcomingTime = outcomingTime.plus(durationBetween);
        }

        //передаем значения в объект типа UDR
        udr.setMsisdn(msisdn);
        UDR.CallDetail incomingCall = new UDR.CallDetail();
        incomingCall.setTotalTime(incomingTime);
        udr.setIncomingCall(incomingCall);

        UDR.CallDetail outcomingCall = new UDR.CallDetail();
        outcomingCall.setTotalTime(outcomingTime);
        udr.setOutcomingCall(outcomingCall);

        return udr;
    }

    /**
     * Данный метод генерирут список с номерами абонентов, абоненты берутся из базы данных
     *
     * @return список нмоеров абонентов
     */
    public List<String> generateMsisdnList() {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        List<String> msisdnList = new ArrayList<>();

        for (Subscriber subscriber : subscribers) {
            msisdnList.add(subscriber.getMsisdn());
        }

        return msisdnList;
    }

    /**
     * Данный метод берет CDR записи из бд, если список не пустой возвращает его
     *
     * @param msisdn номер абонента
     * @return список CDR записей с абонентом в качесвте инициатора
     */
    private ArrayList<CDR> createIncomingList(String msisdn) {
        Optional<ArrayList<CDR>> optionalIncomingList = cdRepository.findIncomingByMsisdn(msisdn);

        if(optionalIncomingList.isEmpty()) {
            throw new IllegalStateException("Incoming calls for " + msisdn + "not found");
        }

        return optionalIncomingList.get();
    }

    /**
     * Данный метод берет CDR записи из бд, если список не пустой возвращает его
     *
     * @param msisdn номера абонента
     * @return список CDR записей с абонентом в качесвте принимающего
     */
    private ArrayList<CDR> createOutcomingList(String msisdn) {
        Optional<ArrayList<CDR>> optionalOutcomingList = cdRepository.findOutcomingByMsisdn(msisdn);

        if(optionalOutcomingList.isEmpty()) {
            throw new IllegalStateException("Outcoming calls for " + msisdn + "not found");
        }

        return optionalOutcomingList.get();
    }
}
