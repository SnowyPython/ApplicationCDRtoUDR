package ru.vatolin.applicationcdrtoudr.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@DataJpaTest
public class CDRepositoryTest {

    @Autowired
    private CDRepository cdRepository;

    /**
     * тестирование поиска msisdn в качестве инициализатора в базе данных
     */
    @Test
    void testFindIncomingByMsisdn() {
        CDR cdr1 = new CDR();
        cdr1.setCallType("01");
        cdr1.setCallerNumber("79998887766");
        cdr1.setReceiverNumber("79995554433");
        cdr1.setStartTime(LocalDateTime.now());
        cdr1.setEndTime(LocalDateTime.now().plusMinutes(10));

        CDR cdr2 = new CDR();
        cdr2.setCallType("01");
        cdr2.setCallerNumber("79998887766");
        cdr2.setReceiverNumber("79992221122");
        cdr2.setStartTime(LocalDateTime.now());
        cdr2.setEndTime(LocalDateTime.now().plusMinutes(5));

        cdRepository.save(cdr1);
        cdRepository.save(cdr2);

        Optional<ArrayList<CDR>> result = cdRepository.findIncomingByMsisdn("79998887766");

        //проверка результатов
        Assertions.assertTrue(result.isPresent(), "Результат не должен быть пустым");
        Assertions.assertEquals(2, result.get().size(), "Должно быть 2 входящих звонка");
        Assertions.assertEquals("79998887766", result.get().get(0).getCallerNumber(), "Номер инициатора должен совпадать");
        Assertions.assertEquals("79998887766", result.get().get(1).getCallerNumber(), "Номер инициатора должен совпадать");
    }

    /**
     * тестирование поиска msisdn в качестве принимающего в базе данных
     */
    @Test
    void testFindOutcomingByMsisdn() {
        CDR cdr1 = new CDR();
        cdr1.setCallType("02");
        cdr1.setCallerNumber("79995554433");
        cdr1.setReceiverNumber("79998887766");
        cdr1.setStartTime(LocalDateTime.now());
        cdr1.setEndTime(LocalDateTime.now().plusMinutes(10));

        CDR cdr2 = new CDR();
        cdr2.setCallType("02");
        cdr2.setCallerNumber("79992221122");
        cdr2.setReceiverNumber("79998887766");
        cdr2.setStartTime(LocalDateTime.now());
        cdr2.setEndTime(LocalDateTime.now().plusMinutes(5));

        cdRepository.save(cdr1);
        cdRepository.save(cdr2);

        Optional<ArrayList<CDR>> result = cdRepository.findOutcomingByMsisdn("79998887766");

        //проверка результатов
        Assertions.assertTrue(result.isPresent(), "Результат не должен быть пустым");
        Assertions.assertEquals(2, result.get().size(), "Должно быть 2 исходящих звонка");
        Assertions.assertEquals("79998887766", result.get().get(0).getReceiverNumber(), "Номер получателя должен совпадать");
        Assertions.assertEquals("79998887766", result.get().get(1).getReceiverNumber(), "Номер получателя должен совпадать");
    }
}
