package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.GlobalTransactionTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalTransactionTicketRepository extends JpaRepository<GlobalTransactionTicket, Long> {
    Optional<GlobalTransactionTicket> findByKey(String key);
}
