package com.aluminate.aluminate_organization_backend.dto;

    import java.math.BigDecimal;

    /**
     * Record class representing a test record.
     * This class is immutable and provides a compact way to define a data carrier.
     *
     * @param id          the unique identifier of the record
     * @param name        the name associated with the record
     * @param description a description providing additional details about the record
     */
    public record TestRecord(Long id, String name, String description) {

        /**
         * Record class representing user details.
         * This class is nested within the `TestRecord` and provides information about the user.
         *
         * @param name        the name of the user
         * @param description a description providing additional details about the user
         */
        public record UserDetails(String name, String description) {}
    }