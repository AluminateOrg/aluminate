package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.AckTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AckTransactionRepository extends JpaRepository<AckTransaction, Long> {
}
