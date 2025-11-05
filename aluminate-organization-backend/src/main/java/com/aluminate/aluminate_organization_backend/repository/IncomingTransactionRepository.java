package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.IncomingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncomingTransactionRepository extends JpaRepository<IncomingTransaction, Long> {
    List<IncomingTransaction> findByStagedFalse();
}
