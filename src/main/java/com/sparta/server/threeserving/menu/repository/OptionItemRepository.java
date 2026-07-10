package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OptionItemRepository extends JpaRepository<OptionItem, UUID> {
}
