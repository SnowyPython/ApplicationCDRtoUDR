package ru.vatolin.applicationcdrtoudr.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

/**
 * Класс сущности UDR
 * Для корректного отображения, соответствующего заданию, в формате JSON, был добавлен вложенный класс CallDetail
 */
public class UDR {
    private String msisdn;
    private CallDetail incomingCall;
    private CallDetail outcomingCall;

    public UDR() {
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    @JsonProperty("incomingCall")
    public CallDetail getIncomingCall() {
        return incomingCall;
    }

    public void setIncomingCall(CallDetail incomingCall) {
        this.incomingCall = incomingCall;
    }

    @JsonProperty("outcomingCall")
    public CallDetail getOutcomingCall() {
        return outcomingCall;
    }

    public void setOutcomingCall(CallDetail outcomingCall) {
        this.outcomingCall = outcomingCall;
    }

    public static class CallDetail {
        private Duration totalTime;

        public CallDetail() {
        }

        @JsonProperty("totalTime")
        public String getTotalTime() {
            return formatDuration(totalTime);
        }

        public void setTotalTime(Duration totalTime) {
            this.totalTime = totalTime;
        }

        //форматируем Duration для корректного отображения в JSON
        private String formatDuration(Duration duration) {
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
}
