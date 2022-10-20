package kitchenpos.application.old;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import kitchenpos.application.ProductService;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import kitchenpos.ui.dto.request.ProductCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional(readOnly = true)
@Service
public class JdbcProductService implements ProductService {
    private final ProductDao productDao;

    public JdbcProductService(final ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    @Override
    public Product create(final ProductCreateRequest request) {
        validateName(request.getName());
        validatePrice(request.getPrice());
        final var product = new Product(request.getName(), request.getPrice());

        return productDao.save(product);
    }

    private void validateName(final String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("유효하지 않은 상품명 :" + name);
        }
    }

    private void validatePrice(final BigDecimal price) {
        if (Objects.isNull(price) || isLessThanZero(price)) {
            throw new IllegalArgumentException("유효하지 않은 가격 : " + price);
        }
    }

    private boolean isLessThanZero(final BigDecimal price) {
        return price.compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    public List<Product> list() {
        return productDao.findAll();
    }
}