package ru.vatolin.applicationcdrtoudr.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.vatolin.applicationcdrtoudr.repository.CDRepository;
import ru.vatolin.applicationcdrtoudr.repository.Subscriber;
import ru.vatolin.applicationcdrtoudr.repository.SubscriberRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Класс инициализации
 * Выполняет необходимые операции при запуске приложения
 *
 * <p>Класс взаимодействует с:
 * <ul>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.repository.CDRepository} — репозиторий для работы с CDR-записями.</li>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.repository.SubscriberRepository} — репозиторий для работы с абонентами.</li>
 *   <li>{@link ru.vatolin.applicationcdrtoudr.service.CDRGeneratorService} — сервис для работы с CDR записями</li>
 * </ul>
 *
 * <p>Для работы используются:
 * <ul>
 *   <li>{@link java.util.Arrays} — для работы с коллекциями.</li>
 *   <li>{@link java.util.List} — для работы с коллекциями.</li>
 * </ul>
 */
@Service
public class InitRunnerService implements CommandLineRunner {
    private final CDRepository cdRepository;
    private final SubscriberRepository subscriberRepository;
    private final CDRGeneratorService cdrGeneratorService;

    public InitRunnerService(CDRepository cdRepository, SubscriberRepository subscriberRepository, CDRGeneratorService cdrGeneratorService) {
        this.cdRepository = cdRepository;
        this.subscriberRepository = subscriberRepository;
        this.cdrGeneratorService = cdrGeneratorService;
    }

    @Override
    public void run(String... args) throws Exception {
        //отчищаем таблицы перед началом работы
        cdRepository.deleteAll();
        subscriberRepository.deleteAll();

        //список абонентов
        List<String> msisdns = Arrays.asList(
                "79001112233", "79101112233", "79201112233", "79301112233", "79401112233",
                "79501112233", "79601112233", "79701112233", "79801112233", "79901112233"
        );

        //сохраняем абонентов в бд
        for (String msisdn : msisdns) {
            Subscriber subscriber = new Subscriber();
            subscriber.setMsisdn(msisdn);
            subscriberRepository.save(subscriber);
        }

        //запуск генерации CDR записей
        cdrGeneratorService.generateCDRecords();
    }
}
