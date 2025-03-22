package ru.vatolin.applicationcdrtoudr.service;

import org.springframework.stereotype.Service;
import ru.vatolin.applicationcdrtoudr.repository.CDR;
import ru.vatolin.applicationcdrtoudr.repository.CDRepository;
import ru.vatolin.applicationcdrtoudr.repository.Subscriber;
import ru.vatolin.applicationcdrtoudr.repository.SubscriberRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Данный класс выступает в качестве сервиса для работы с CDR записями.
 * Основная задача данного метода генерация CDR записей в хронологическом порядке и добавление их в бд
 *
 * <p>Основные методы:
 * <ul>
 *   <li>{@link #generateCDRecords()} — генерирует случайные CDR записи и помещает их в бд.</li>
 *   <li>{@link #generateCDReport(String, LocalDateTime, LocalDateTime)} — генерирует CDR-отчет за указанный год.</li>
 * </ul>
 *
 * <p>Вспомогательные методы:
 * <ul>
 *   <li>{@link #calculateMaxStartTime(LocalDateTime, LocalDateTime, int)} — генерирует верхнюю границу времени.</li>
 *   <li>{@link #generateRandomDateInRange(LocalDateTime, LocalDateTime)} — генерирует случайную дату в заданом диапазоне.</li>
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
 *   <li>{@link java.time.LocalDateTime} — для работы со временем.</li>
 *   <li>{@link java.time.temporal.ChronoUnit} — для расчета промежутков времени.</li>
 *   <li>{@link java.util.List} — для работы с коллекциями.</li>
 *   <li>{@link java.util.Random} — для генерации случайных значений.</li>
 *   <li>{@link java.io.BufferedWriter} — для записи в файл.</li>
 *   <li>{@link java.nio.file.Files} — вспомогательный класс для работы с файлами.</li>
 *   <li>{@link java.nio.file.Path} — для работы с путями файлов.</li>
 *   <li>{@link java.nio.file.Paths} — вспомогательный класс для работы с путями файлов.</li>
 * </ul>
 */
@Service
public class CDRGeneratorService {
    private final CDRepository cdRepository;
    private final SubscriberRepository subscriberRepository;

    public CDRGeneratorService(CDRepository cdRepository, SubscriberRepository subscriberRepository) {
        this.cdRepository = cdRepository;
        this.subscriberRepository = subscriberRepository;
    }

    /**
     * Данный метод занимается генерацией случайного количества CDR записей со случайной длительностью звонка
     * (ограничения генерации представлены в виде констант в классе CDRGeneratorService).
     * Метод сам заботиться о корректности сгенерированных данных.
     * Сгенерированные записи сразу заносятся в бд
     */
    public void generateCDRecords() {
        final int MIN_COUNT_RECORDS = 1000;
        final int MAX_COUNT_RECORDS = 2000;
        final int MAX_CALL_TIME_IN_MINUTES = 120;

        Random random = new Random();

        //получаем абонентов из базы данных
        List<Subscriber> subscribers = subscriberRepository.findAll();

        //получаем текщие время и время год назад
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = currentDateTime.minusYears(1);

        //генерируем случайное количество записей
        int countOfRecords = random.nextInt(MIN_COUNT_RECORDS, MAX_COUNT_RECORDS);
        //ограничитель время последнего окончания
        LocalDateTime lastEndTime = startDateTime;

        //цикл генерации записей
        for (int i = 0; i < countOfRecords; i++) {
            //генерация верхней границы времени начала
            LocalDateTime maxStartTime = calculateMaxStartTime(lastEndTime, currentDateTime, countOfRecords - i);

            //случайно получаем тип звонка (true - 01, false - 02)
            String callType = (random.nextBoolean()) ? "01" : "02";
            //берем номер случайного абонента для инициатора
            String callerNumber = subscribers.get(random.nextInt(subscribers.size())).getMsisdn();
            //номер принимающего абонента
            String receiverNumber;
            //время начала звонка
            LocalDateTime startTime = generateRandomDateInRange(lastEndTime, maxStartTime);
            //время окончания звонка
            LocalDateTime endTime = startTime.plusMinutes(random.nextInt(MAX_CALL_TIME_IN_MINUTES));

            //проверка корректности времени окончания
            if (endTime.isAfter(currentDateTime)) {
                endTime = currentDateTime;
            }

            //проверяем, чтобы номера инициатора и принимающего не совпадали
            do {
                receiverNumber = subscribers.get(random.nextInt(subscribers.size())).getMsisdn();
            } while(receiverNumber.equals(callerNumber));

            //создаем CDR объкт и помещаем его в бд
            CDR cdr = new CDR();
            cdr.setCallType(callType);
            cdr.setCallerNumber(callerNumber);
            cdr.setReceiverNumber(receiverNumber);
            cdr.setStartTime(startTime);
            cdr.setEndTime(endTime);
            cdRepository.save(cdr);

            //запоминаем время окончания
            lastEndTime = endTime;
        }
    }

    /**
     * Данный метод формирует CDR отчет в формате csv и сохраняет их в src/main/resources/reports
     *
     * @param msisdn номер абонента
     * @param startDateTime начало периода, за который хотим получить отчет
     * @param endDateTime конец периода, за который хотим получить отчет
     * @return UUID отчета
     * @throws RuntimeException выбрасывается, если пользователь не найдет, если не найдено записей за заданый период, если произошла ошибка при генерации csv файла
     */
    public String generateCDReport(String msisdn, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        //проверяем существует ли пользователь с таким номером
        if (msisdnIsNotExist(msisdn)) {
            throw new RuntimeException("Subscriber " + msisdn + " is not exist");
        }

        //список пользователь в заданом периоде
        List<CDR> CDRList = createCDRListInRange(msisdn, startDateTime, endDateTime);

        //проверяем, чтобы список был не пустым
        if (CDRList.isEmpty()) {
            throw new RuntimeException("No records found for this period.");
        }

        String reportId = UUID.randomUUID().toString();
        String fileName = msisdn + "_" + reportId + ".csv";
        Path filePath = Paths.get("src/main/resources/reports", fileName);

        //генерация csv
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE)) {
            writer.write("callType,callerNumber,receiverNumber,startTime,endTime\n");

            for(CDR cdr : CDRList) {
                writer.write(String.format("%s, %s, %s, %s, %s\n",
                        cdr.getCallType(),
                        cdr.getCallerNumber(),
                        cdr.getReceiverNumber(),
                        cdr.getStartTime(),
                        cdr.getEndTime()));
            }
        } catch (IOException e) {
            throw new RuntimeException("csv generation failed");
        }

        return reportId;
    }

    /**
     * Данный метод вычисляет верхнюю границу времени начала, с каждым прогоном цикла, в котором выполняется данный метод,
     * верхняя граница приближается к currentDateTime.
     *
     * @param lastEndTime время последнего окончания
     * @param currentDateTime текущее время (полученное в начале generateCDRecords())
     * @param remainingRecords число записей, которые необходимо сгенерировать
     * @return верхняя граница времени начала
     */
    private LocalDateTime calculateMaxStartTime(LocalDateTime lastEndTime, LocalDateTime currentDateTime, int remainingRecords) {
        long totalSeconds = ChronoUnit.SECONDS.between(lastEndTime, currentDateTime);
        long secondsPerRecord = totalSeconds / remainingRecords;

        return lastEndTime.plusSeconds(secondsPerRecord);
    }

    /**
     * Данный метод генерирует случайную дату в заданном диапазоне
     *
     * @param startDate начало диапазона
     * @param endDate конец диапазона
     * @return случайное время в заданном диапазоне
     */
    private LocalDateTime generateRandomDateInRange(LocalDateTime startDate, LocalDateTime endDate) {
        Random random = new Random();

        long secondBetween = ChronoUnit.SECONDS.between(startDate, endDate);
        long randomSecond = random.nextLong(secondBetween);
        return startDate.plusSeconds(randomSecond);
    }

    /**
     * Данный метод формирует список CDR записей для заданного пользователя за определенный период
     *
     * @param msisdn номер абонента
     * @param startDateTime начало периода, в котором будем отбирать записи
     * @param endDateTime конец периода, в котором будем отбирать записи
     * @return список CDR записей за необходимый период
     * @throws IllegalStateException выбрасывается, если записи с заданым абонентом не были найдены в бд
     */
    private ArrayList<CDR> createCDRListInRange(String msisdn, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Optional<ArrayList<CDR>> optionalIncomingList = cdRepository.findIncomingByMsisdn(msisdn);

        if(optionalIncomingList.isEmpty()) {
            throw new IllegalStateException("Incoming calls for " + msisdn + " not found");
        }

        List<CDR> fuelList = optionalIncomingList.get();
        ArrayList<CDR> resultList = new ArrayList<>();

        for(CDR cdr : fuelList) {
            if (cdr.getStartTime().isAfter(startDateTime) && cdr.getStartTime().isBefore(endDateTime)) {
                resultList.add(cdr);
            }
        }

        return resultList;
    }

    /**
     * Данный метод проверяет наличие заданного абонента в бд
     *
     * @param msisdn номер абонента
     * @return true - если абонента не существует, false - в обратном случае
     */
    private boolean msisdnIsNotExist(String msisdn) {
        List<Subscriber> subscribers = subscriberRepository.findAll();

        for(Subscriber subscriber : subscribers) {
            if (subscriber.getMsisdn().equals(msisdn)) {
                return false;
            }
        }
        return true;
    }
}
