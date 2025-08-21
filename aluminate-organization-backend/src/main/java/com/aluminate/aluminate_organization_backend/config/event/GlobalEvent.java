package com.aluminate.aluminate_organization_backend.config.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GlobalEvent {
    private String eventId;            // uuid
    private String eventType;          // AdminUpdated, OrganizationUpdated, ...
    private int version;
    private Instant occurredAt;
    private String organizationName;   // key used for partitioning + matching
    private Map<String, Object> data;  // flexible payload (or create typed payloads)
    // getters/setters + no-arg ctor
}
