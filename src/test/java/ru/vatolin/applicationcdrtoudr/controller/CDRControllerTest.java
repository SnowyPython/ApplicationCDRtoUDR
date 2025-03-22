package ru.vatolin.applicationcdrtoudr.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.vatolin.applicationcdrtoudr.service.CDRGeneratorService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CDRControllerTest {
    @Mock
    private CDRGeneratorService cdrGeneratorService;

    @InjectMocks
    private CDRController cdrController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cdrController).build();
    }

    /**
     * проверяем вызов генерации CDR отчета
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR() throws Exception {
        String reportId = UUID.randomUUID().toString();

        when(cdrGeneratorService.generateCDReport("79001002030", LocalDateTime.parse("2025-01-01T00:00:00"),
                LocalDateTime.parse("2025-03-01T00:00:00"))).thenReturn(reportId);

        mockMvc.perform(get("/cdr/generate/79001002030?startDate=2025-01-01T00:00:00&endDate=2025-03-01T00:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.successfully").value(reportId));
    }

    /**
     * проверяем вызов генерации CDR отчета без аргумента начальной даты
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR_noStartDate() throws Exception {
        mockMvc.perform(get("/cdr/generate/79001002030?endDate=2025-03-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    /**
     * проверяем вызов генерации CDR отчета без аргумента конечной даты
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR_noEndDate() throws Exception {
        mockMvc.perform(get("/cdr/generate/79001002030?startDate=2025-01-01T00:00:00"))
                .andExpect(status().isBadRequest());
    }

    /**
     * проверяем вызов генерации CDR отчета с некорректным аргументом начальной даты
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR_invalidStartDate() throws Exception {
        mockMvc.perform(get("/cdr/generate/79001002030?startDate=2025.01.01&endDate=2025-03-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid target date format. Expected format: YYYY-MM-DDTHH:mm:ss"));
    }

    /**
     * проверяем вызов генерации CDR отчета с некорректным аргументом конечной даты
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR_invalidEndDate() throws Exception {
        mockMvc.perform(get("/cdr/generate/79001002030?startDate=2025-01-01T00:00:00&endDate=2025.03.01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid target date format. Expected format: YYYY-MM-DDTHH:mm:ss"));;
    }

    /**
     * проверяем вызов генерации CDR отчета, если данного пользователя не существует
     * @throws Exception выбрасывает perform
     */
    @Test
    void testGenerateCDR_niMsisdn() throws Exception {
        String msisdn = "79998887766";

        when(cdrGeneratorService.generateCDReport(msisdn, LocalDateTime.parse("2025-01-01T00:00:00"),
                LocalDateTime.parse("2025-03-01T00:00:00"))).thenThrow(RuntimeException.class);

        mockMvc.perform(get("/cdr/generate/79998887766?startDate=2025-01-01T00:00:00&endDate=2025-03-01T00:00:00"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("No CDR for " + msisdn));
    }
}
