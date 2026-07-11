package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OptionItemRepository extends JpaRepository<OptionItem, UUID> {

    // N+1 문제를 해결하기 위한 Fetch Join
    // 옵션 그룹, 해당 그룹의 전체 아이템 (minSelect 계산), 가게, 사용자(점주) 정보 가져오기
    @Query("SELECT DISTINCT oi FROM OptionItem oi JOIN FETCH oi.optionGroup og JOIN FETCH og.optionItemList JOIN FETCH og.store s JOIN FETCH s.owner WHERE oi.id IN :optionItemIds")
    List<OptionItem> findAllWithGroupAndStoreByIdIn(@Param("optionItemIds") Iterable<UUID> optionItemIds);
}
