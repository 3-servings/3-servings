package com.sparta.server.threeserving.menu.repository;

import com.sparta.server.threeserving.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}
