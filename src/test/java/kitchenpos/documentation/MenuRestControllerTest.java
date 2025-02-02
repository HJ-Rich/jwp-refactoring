package kitchenpos.documentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Product;
import kitchenpos.domain.vo.MenuPrice;
import kitchenpos.domain.vo.ProductPrice;
import kitchenpos.ui.dto.request.MenuCreateRequest;
import kitchenpos.ui.dto.request.MenuProductRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

class MenuRestControllerTest extends DocumentationTest {
    private static final String MENU_API_URL = "/api/menus";

    private final Product productA = new Product(1L, "까르보치킨 한 마리", ProductPrice.from("20000.00"));

    @DisplayName("POST " + MENU_API_URL)
    @Test
    void create() {
        final var name = "까르보 한 마리 + 짜장 한 마리";
        final var price = new BigDecimal("38000.00");
        final var menuGroupId = 1L;
        final var menuGroup = new MenuGroup(1L, "두 마리 메뉴");
        given(menuService.create(any()))
                .willReturn(new Menu(1L, name, MenuPrice.from(price), menuGroup.getId(),
                                List.of(
                                        new MenuProduct(1L, null, productA, 1L),
                                        new MenuProduct(2L, null, new Product(2L, "짜장 한 마리", ProductPrice.from("18000.00")),
                                                1L)
                                )
                        )
                );

        docsGiven
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(new MenuCreateRequest(name, price, menuGroupId,
                        List.of(new MenuProductRequest(1L, 1L), new MenuProductRequest(2L, 1L))))
                .when().post(MENU_API_URL)
                .then().log().all()
                .apply(document("menus/create",
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("메뉴 이름"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).description("메뉴 가격"),
                                fieldWithPath("menuGroupId").type(JsonFieldType.NUMBER).description("메뉴 그룹 아이디"),
                                fieldWithPath("menuProducts.[].productId").type(JsonFieldType.NUMBER)
                                        .description("프로덕트 아이디"),
                                fieldWithPath("menuProducts.[].quantity").type(JsonFieldType.NUMBER)
                                        .description("프로덕트 수량")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("메뉴 아이디"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("메뉴 이름"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER).description("메뉴 가격"),
                                fieldWithPath("menuGroupId").type(JsonFieldType.NUMBER).description("메뉴 그룹 아이디"),
                                fieldWithPath("menuProducts.[].seq").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 seq"),
                                fieldWithPath("menuProducts.[].menuId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 메뉴 아이디"),
                                fieldWithPath("menuProducts.[].productId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 프로덕트 아이디"),
                                fieldWithPath("menuProducts.[].quantity").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 프로덕트 수량")
                        )
                ))
                .statusCode(HttpStatus.CREATED.value());
    }

    @DisplayName("GET " + MENU_API_URL)
    @Test
    void list() {
        final var menuGroup = new MenuGroup(1L, "한 마리 메뉴");
        given(menuService.list())
                .willReturn(List.of(
                                new Menu(1L, "까르보 한 마리", MenuPrice.from(new BigDecimal("18000.00")), menuGroup.getId(),
                                        List.of(
                                                new MenuProduct(1L, null, productA, 1L)
                                        )
                                ),
                                new Menu(2L, "까르보 두 마리", MenuPrice.from(new BigDecimal("35000.00")), menuGroup.getId(),
                                        List.of(
                                                new MenuProduct(2L, null, productA, 2L)
                                        )
                                )
                        )
                );

        docsGiven
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get(MENU_API_URL)
                .then().log().all()
                .apply(document("menus/list",
                        responseFields(
                                fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("메뉴 아이디"),
                                fieldWithPath("[].name").type(JsonFieldType.STRING).description("메뉴 이름"),
                                fieldWithPath("[].price").type(JsonFieldType.NUMBER).description("메뉴 가격"),
                                fieldWithPath("[].menuGroupId").type(JsonFieldType.NUMBER).description("메뉴 그룹 아이디"),
                                fieldWithPath("[].menuProducts.[].seq").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 seq"),
                                fieldWithPath("[].menuProducts.[].menuId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 메뉴 아이디"),
                                fieldWithPath("[].menuProducts.[].productId").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 프로덕트 아이디"),
                                fieldWithPath("[].menuProducts.[].quantity").type(JsonFieldType.NUMBER)
                                        .description("메뉴 프로덕트 프로덕트 수량")
                        )
                ))
                .statusCode(HttpStatus.OK.value());
    }
}
