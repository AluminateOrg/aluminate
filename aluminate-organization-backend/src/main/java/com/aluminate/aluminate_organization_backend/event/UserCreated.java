package com.aluminate.aluminate_organization_backend.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event class representing the creation of a user.
 * This class is used to encapsulate information about a newly created user.
 * It is annotated with Lombok annotations to reduce boilerplate code.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreated {

    /**
     * The name of the user.
     */
    private String name;

    /**
     * A description providing additional details about the user.
     */
    private String description;
}