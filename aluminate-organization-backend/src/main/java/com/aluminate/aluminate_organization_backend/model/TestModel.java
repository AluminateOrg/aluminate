package com.aluminate.aluminate_organization_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing the `test_table` in the database.
 * This class is annotated with JPA and Lombok annotations to simplify persistence and boilerplate code.
 */
@Entity
@Table(name = "test_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestModel {

    /**
     * The unique identifier for the entity.
     * This field is the primary key and is auto-generated using the IDENTITY strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name associated with the entity.
     */
    private String name;

    /**
     * A description providing additional details about the entity.
     */
    private String description;

    // Additional fields and methods can be added as needed
}