package br.com.autocarehub.interfaces.rest.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class RestMapperSupportTest {

    @Test
    void shouldHandleEmptyItemsAndInvalidSizes() {
        assertThat(RestMapperSupport.page(List.<String>of(), 0, 10)).isEmpty();
        assertThat(RestMapperSupport.page(List.of("a", "b"), null, null)).containsExactly("a", "b");
        assertThat(RestMapperSupport.totalPages(0, 10)).isZero();
        assertThat(RestMapperSupport.totalPages(5, 0)).isZero();
        assertThat(RestMapperSupport.totalPages(5, null)).isEqualTo(1);
        assertThat(RestMapperSupport.totalPages(5, 2)).isEqualTo(3);
    }
}
