package com.sparta.server.threeserving.menu.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // 단방향: Menu(N) -> MenuCategory(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id", nullable = false)
    private MenuCategory menuCategory;

    // 단방향: Menu(N) -> Store(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "is_description_ai_generated", nullable = false)
    private boolean isDescriptionAiGenerated = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuStatus status = MenuStatus.AVAILABLE;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    // 양방향: Menu(1) <-> 매핑 테이블(N) 참조
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuOptionGroup> menuOptionGroups = new ArrayList<>();

    // store 반영 후 다시 수정
    @Builder
    public Menu(MenuCategory menuCategory, Store store, String name, int price,
                String description, boolean isDescriptionAiGenerated, int displayOrder) {
        this.menuCategory = menuCategory;
        this.store = store;
        this.name = name;
        this.price = price;
        this.description = description;
        this.isDescriptionAiGenerated = isDescriptionAiGenerated;
        this.displayOrder = displayOrder;
    }

    public void update(MenuCategory menuCategory, String name, int price, String description, boolean isDescriptionAiGenerated, MenuStatus status) {
        this.menuCategory = menuCategory;
        this.name = name;
        this.price = price;
        this.description = description;
        this.isDescriptionAiGenerated = isDescriptionAiGenerated;
        this.status = status;
    }

    public void updateStatus(MenuStatus status) {
        this.status = status;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}
