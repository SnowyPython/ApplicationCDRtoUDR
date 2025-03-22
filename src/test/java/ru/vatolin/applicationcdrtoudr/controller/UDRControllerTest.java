package ru.vatolin.applicationcdrtoudr.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.vatolin.applicationcdrtoudr.repository.UDR;
import ru.vatolin.applicationcdrtoudr.service.UDRGeneratorService;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UDRControllerTest {
    @Mock
    private UDRGeneratorService udrGeneratorService;

    @InjectMocks
    private UDRController udrController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(udrController).build();
    }

    /**
     * проверяем вызов генерации UDR очета за месяц
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReport_periodMonth() throws Exception {
        String msisdn = "79998887766";
        int numberOfMonth = 1;

        UDR udr = new UDR();
        udr.setMsisdn(msisdn);
        UDR.CallDetail incomingCall = new UDR.CallDetail();
        incomingCall.setTotalTime(Duration.ofMinutes(60));
        udr.setIncomingCall(incomingCall);

        UDR.CallDetail outcomingCall = new UDR.CallDetail();
        outcomingCall.setTotalTime(Duration.ofMinutes(60));
        udr.setOutcomingCall(outcomingCall);

        when(udrGeneratorService.generateUDReportForMonth(msisdn, numberOfMonth)).thenReturn(udr);

        //проверяем получение нужного ответа и корректных данных в json
        mockMvc.perform(get("/udr/report/79998887766?period=M&numberOfMonth=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msisdn").value(msisdn))
                .andExpect(jsonPath("$.incomingCall.totalTime").value("01:00:00"))
                .andExpect(jsonPath("$.outcomingCall.totalTime").value("01:00:00"));
    }

    /**
     * тестируем получение корректного ответа при неверном параматре номера месяца
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReport_periodMonth_invalidMonthMoreThan12() throws Exception {
        mockMvc.perform(get("/udr/report/79998887766?period=M&numberOfMonth=13"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid month number"));
    }

    /**
     * тестируем получение корректного ответа при неверном параматре номера месяца
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReport_periodMonth_invalidMonthLessThan0() throws Exception {
        mockMvc.perform(get("/udr/report/79998887766?period=M&numberOfMonth=-1"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Invalid month number"));
    }

    /**
     * проверяем вызов генерации UDR очета за год
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReport_periodYear() throws Exception {
        String msisdn = "79998887766";

        UDR udr = new UDR();
        udr.setMsisdn(msisdn);
        UDR.CallDetail incomingCall = new UDR.CallDetail();
        incomingCall.setTotalTime(Duration.ofMinutes(60));
        udr.setIncomingCall(incomingCall);

        UDR.CallDetail outcomingCall = new UDR.CallDetail();
        outcomingCall.setTotalTime(Duration.ofMinutes(60));
        udr.setOutcomingCall(outcomingCall);

        when(udrGeneratorService.generateUDReportForYear(msisdn)).thenReturn(udr);

        mockMvc.perform(get("/udr/report/79998887766?period=Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msisdn").value(msisdn))
                .andExpect(jsonPath("$.incomingCall.totalTime").value("01:00:00"))
                .andExpect(jsonPath("$.outcomingCall.totalTime").value("01:00:00"));
    }

    /**
     * тестируем получение корректного ответа при неверном параматре периода
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReport_invalidPeriod() throws Exception {
        mockMvc.perform(get("/udr/report/79998887766?period=invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid period parameter"));
    }

    /**
     * проверяем вызов генерации UDR очета для всех абонентов за заданный месяц
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReportForEveryone() throws Exception {
        UDR udr1 = new UDR();
        udr1.setMsisdn("79998887766");
        UDR.CallDetail incomingCall = new UDR.CallDetail();
        incomingCall.setTotalTime(Duration.ofMinutes(60));
        udr1.setIncomingCall(incomingCall);

        UDR.CallDetail outcomingCall = new UDR.CallDetail();
        outcomingCall.setTotalTime(Duration.ofMinutes(60));
        udr1.setOutcomingCall(outcomingCall);

        UDR udr2 = new UDR();
        udr2.setMsisdn("79995554433");
        UDR.CallDetail incomingCall1 = new UDR.CallDetail();
        incomingCall1.setTotalTime(Duration.ofMinutes(60));
        udr2.setIncomingCall(incomingCall1);

        UDR.CallDetail outcomingCall1 = new UDR.CallDetail();
        outcomingCall1.setTotalTime(Duration.ofMinutes(60));
        udr2.setOutcomingCall(outcomingCall1);

        List<String> list = List.of("79998887766", "79995554433");
        when(udrGeneratorService.generateMsisdnList()).thenReturn(list);
        when(udrGeneratorService.generateUDReportForMonth("79998887766", 1)).thenReturn(udr1);
        when(udrGeneratorService.generateUDReportForMonth("79995554433", 1)).thenReturn(udr2);

        //проверяем получение нужного ответа и корректных данных в json
        mockMvc.perform(get("/udr/report/all?numberOfMonth=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].msisdn").value("79998887766"))
                .andExpect(jsonPath("$[0].incomingCall.totalTime").value("01:00:00"))
                .andExpect(jsonPath("$[0].outcomingCall.totalTime").value("01:00:00"))
                .andExpect(jsonPath("$[1].msisdn").value("79995554433"))
                .andExpect(jsonPath("$[1].incomingCall.totalTime").value("01:00:00"))
                .andExpect(jsonPath("$[1].outcomingCall.totalTime").value("01:00:00"));
    }

    /**
     * проверяем вызов генерации UDR очета для всех абонентов за заданный месяц когда нет отчетов за заданный меясяц
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateUDReportForEveryone_whenNoReports() throws Exception {
        List<String> list = List.of("79998887766", "79995554433");
        when(udrGeneratorService.generateMsisdnList()).thenReturn(list);
        when(udrGeneratorService.generateUDReportForMonth("79998887766", 1)).thenThrow(new RuntimeException());
        when(udrGeneratorService.generateUDReportForMonth("79995554433", 1)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/udr/report/all?numberOfMonth=1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No records for this month"));
    }
}
