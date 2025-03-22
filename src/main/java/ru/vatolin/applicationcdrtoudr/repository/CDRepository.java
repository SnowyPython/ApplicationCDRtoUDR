package ru.vatolin.applicationcdrtoudr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.Optional;

public interface CDRepository extends JpaRepository<CDR, Long> {
    @Query(value = "select * from cdr where caller = :msisdn", nativeQuery = true)
    Optional<ArrayList<CDR>> findIncomingByMsisdn(String msisdn);

    @Query(value = "select * from cdr where receiver = :msisdn", nativeQuery = true)
    Optional<ArrayList<CDR>> findOutcomingByMsisdn(String msisdn);
}
