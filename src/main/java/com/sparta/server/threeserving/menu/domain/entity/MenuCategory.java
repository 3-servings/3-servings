package com.sparta.server.threeserving.menu.domain.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class MenuCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // 단방향: MenuCategory(N) -> Store(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    // store 반영 후 다시 수정
    @Builder
    public MenuCategory(Store store, String name, int displayOrder) {
        this.store = store;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public void update(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }
}
