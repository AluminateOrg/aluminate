package com.aluminate.aluminate_organization_backend.service;

    import org.springframework.stereotype.Service;

    /**
     * Service class responsible for handling greeting-related logic.
     * This class is annotated with @Service to indicate that it is a Spring service component.
     */
    @Service
    public class HelloService {

        /**
         * Returns a greeting message.
         *
         * @return a String containing the greeting message "Hello World!"
         */
        public String sayHello() {
            return "Hello World!";
        }
    }