package kitchenpos.application;

import static kitchenpos.KitchenPosFixtures.까르보치킨_생성요청;
import static kitchenpos.KitchenPosFixtures.짜장치킨_생성요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kitchenpos.domain.Product;
import kitchenpos.exception.badrequest.ProductNameDuplicateException;
import kitchenpos.exception.badrequest.ProductNameInvalidException;
import kitchenpos.exception.badrequest.ProductPriceInvalidException;
import kitchenpos.ui.dto.request.ProductCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

class ProductServiceTest extends ServiceTest {
    @Autowired
    private ProductService productService;

    @DisplayName("프로덕트를 생성할 수 있다")
    @Test
    void create() {
        // given
        final var productRequest = new ProductCreateRequest("까르보치킨", new BigDecimal("20000.00"));

        // when
        final var actual = productService.create(productRequest);

        // then
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual.getName()).isEqualTo(productRequest.getName()),
                () -> assertThat(actual.getPrice()).isEqualTo(productRequest.getPrice())
        );
    }

    @MethodSource("createProductFailCases")
    @ParameterizedTest(name = "{0}")
    void create_fail_cases(final String description, final String name, final BigDecimal price, final Class<?> clazz) {
        // given
        final var productRequest = new ProductCreateRequest(name, price);

        // when & then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(clazz);
    }

    private static Stream<Arguments> createProductFailCases() {
        return Stream.of(
                Arguments.of("Product 생성 시, name이 null이면 예외가 발생한다",
                        null, new BigDecimal("20000.00"), ProductNameInvalidException.class),
                Arguments.of("Product 생성 시, name이 비어있으면 예외가 발생한다",
                        " ", new BigDecimal("20000.00"), ProductNameInvalidException.class),
                Arguments.of("Product 생성 시, name이 비어있으면 예외가 발생한다",
                        "", new BigDecimal("20000.00"), ProductNameInvalidException.class),
                Arguments.of("Product 생성 시, price가 null이면 예외가 발생한다",
                        "상품명", null, ProductPriceInvalidException.class),
                Arguments.of("Product 생성 시, price가 0 보다 작으면 예외가 발생한다",
                        "상품명", new BigDecimal("-10000.00"), ProductPriceInvalidException.class)
        );
    }

    @DisplayName("Product 생성 시, 이미 존재하는 name일 경우 예외가 발생한다")
    @Test
    void should_fail_when_name_is_duplicate() {
        // given
        final var productRequest = new ProductCreateRequest("까르보치킨", new BigDecimal("20000.00"));
        productService.create(productRequest);

        // when & then
        assertThatThrownBy(() -> productService.create(productRequest))
                .isInstanceOf(ProductNameDuplicateException.class);
    }

    @DisplayName("전체 프로덕트를 조회할 수 있다")
    @Test
    void list() {
        // given
        productService.create(까르보치킨_생성요청);
        productService.create(짜장치킨_생성요청);

        // when
        final var products = productService.list();
        final var prices = products.stream()
                .map(Product::getPrice)
                .collect(Collectors.toList());

        // then
        assertAll(
                () -> assertThat(products.size()).isEqualTo(2),
                () -> assertThat(products).extracting("id")
                        .containsExactly(1L, 2L),
                () -> assertThat(products).extracting("name")
                        .containsExactly(까르보치킨_생성요청.getName(), 짜장치킨_생성요청.getName()),
                () -> assertThat(prices).containsExactly(까르보치킨_생성요청.getPrice(), 짜장치킨_생성요청.getPrice())
        );
    }
}
