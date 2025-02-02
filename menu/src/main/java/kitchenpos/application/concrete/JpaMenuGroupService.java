package kitchenpos.application.concrete;

import java.util.List;
import kitchenpos.application.MenuGroupService;
import kitchenpos.domain.MenuGroup;
import kitchenpos.repository.MenuGroupRepository;
import kitchenpos.ui.dto.request.MenuGroupCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class JpaMenuGroupService implements MenuGroupService {
    private final MenuGroupRepository menuGroupRepository;

    public JpaMenuGroupService(final MenuGroupRepository menuGroupRepository) {
        this.menuGroupRepository = menuGroupRepository;
    }

    @Transactional
    @Override
    public MenuGroup create(final MenuGroupCreateRequest request) {
        final var newMenuGroup = new MenuGroup(request.getName());

        return menuGroupRepository.save(newMenuGroup);
    }

    @Override
    public List<MenuGroup> list() {
        return menuGroupRepository.findAll();
    }
}
