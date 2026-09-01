package br.com.autocarehub.interfaces.rest.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

final class RestMapperSupport {

    private RestMapperSupport() {}

    static OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }

    static <T> List<T> page(List<T> items, Integer page, Integer size) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? items.size() : size;
        if (pageSize <= 0 || items.isEmpty()) {
            return List.of();
        }
        int fromIndex = Math.min(pageNumber * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    static int totalPages(int totalItems, Integer size) {
        int pageSize = size == null ? totalItems : size;
        if (pageSize <= 0 || totalItems == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }
}
