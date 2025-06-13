package com.aluminate.aluminate_organization_backend.repository;

import com.aluminate.aluminate_organization_backend.model.TestModel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing `TestModel` entities.
 * This interface extends JpaRepository, providing CRUD operations and additional JPA functionality.
 */
public interface testRepository extends JpaRepository<TestModel, Long> {
    // Additional query methods can be defined here if needed.
}