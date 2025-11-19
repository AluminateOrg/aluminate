package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.StagedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StagedTransactionRepository extends JpaRepository<StagedTransaction, Long> {

    @Query("SELECT s FROM StagedTransaction s WHERE s.createdAt BETWEEN :start AND :end AND s.key IS NULL AND s.globalTransactionTicket IS NULL")
    List<StagedTransaction> findStagedTransactionsBetweenDatesUnticketed(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
    List<StagedTransaction> findByGlobalTransactionTicketId(Long globalTransactionTicketId);


}
