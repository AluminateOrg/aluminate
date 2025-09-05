package com.aluminate.aluminate_organization_backend.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PageResponse<T> {
    List<T> items;
    String nextCursor; // ISO-8601 Instant or null
}
