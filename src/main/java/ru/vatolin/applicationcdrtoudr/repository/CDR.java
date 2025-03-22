package ru.vatolin.applicationcdrtoudr.repository;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cdr")
public class CDR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "call_type")
    private String callType;
    @Column(name = "caller")
    private String callerNumber;
    @Column(name = "receiver")
    private String receiverNumber;
    @Column(name = "start_time")
    private LocalDateTime startTime;
    @Column(name = "end_time")
    private LocalDateTime endTime;

    public CDR() {
    }

    public CDR(Long id, String callType, String callerNumber, String receiverNumber, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.callType = callType;
        this.callerNumber = callerNumber;
        this.receiverNumber = receiverNumber;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getReceiverNumber() {
        return receiverNumber;
    }

    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
