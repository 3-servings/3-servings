package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OptionItemRepository extends JpaRepository<OptionItem, UUID> {

    // 변경할 여러 id 한 번에 조회
    List<OptionItem> findByIdIn(List<UUID> optionItemIds);

}
