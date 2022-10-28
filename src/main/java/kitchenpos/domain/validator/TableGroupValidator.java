package kitchenpos.domain.validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class TableGroupValidator {
    private static final int MINIMUM_TABLE_GROUP_SIZE = 2;
    private static final List<String> ACTIVE_ORDER_STATUS = List.of(
            OrderStatus.COOKING.name(),
            OrderStatus.MEAL.name()
    );

    private final OrderTableRepository orderTableRepository;
    private final OrderRepository orderRepository;

    public TableGroupValidator(final OrderTableRepository orderTableRepository, final OrderRepository orderRepository) {
        this.orderTableRepository = orderTableRepository;
        this.orderRepository = orderRepository;
    }

    public void validateGroup(final TableGroup tableGroup) {
        final var requestIds = tableGroup.getOrderTableIds();
        validateRequestIdsSize(requestIds);

        final var orderTables = orderTableRepository.findAllByIdInAndEmptyIsTrueAndTableGroupIdIsNull(requestIds);
        validateFoundTableSize(requestIds, orderTables);
    }

    private void validateRequestIdsSize(final Set<Long> requestIds) {
        if (CollectionUtils.isEmpty(requestIds) || requestIds.size() < MINIMUM_TABLE_GROUP_SIZE) {
            throw new IllegalArgumentException();
        }
    }

    private void validateFoundTableSize(final Set<Long> orderTableIds, final List<OrderTable> orderTables) {
        if (orderTableIds.size() != orderTables.size()) {
            throw new IllegalArgumentException();
        }
    }

    public void validateUnGroup(final TableGroup tableGroup) {
        final var tableGroupId = tableGroup.getId();
        final var orderTables = orderTableRepository.findAllByTableGroupId(tableGroupId);
        validateTableGroupId(orderTables, tableGroupId);

        final var orderTableIds = extractTableIds(orderTables);
        validateAllOrdersInGroupedTableAreComplete(orderTableIds);
    }


    private void validateTableGroupId(final List<OrderTable> orderTables, final Long tableGroupId) {
        if (CollectionUtils.isEmpty(orderTables)) {
            throw new IllegalArgumentException("유효하지 않은 그룹 아이디 : " + tableGroupId);
        }
    }

    private List<Long> extractTableIds(final List<OrderTable> orderTables) {
        return orderTables.stream()
                .map(OrderTable::getId)
                .collect(Collectors.toList());
    }

    private void validateAllOrdersInGroupedTableAreComplete(final List<Long> orderTableIds) {
        if (orderRepository.existsByOrderTableIdInAndOrderStatusIn(orderTableIds, ACTIVE_ORDER_STATUS)) {
            throw new IllegalArgumentException();
        }
    }
}
