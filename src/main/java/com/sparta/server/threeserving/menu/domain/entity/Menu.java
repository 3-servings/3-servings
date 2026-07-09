package com.sparta.server.threeserving.menu.domain.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_menu")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE p_menu SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 단방향: Menu(N) -> MenuCategory(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_category_id", nullable = false)
    private MenuCategory menuCategory;

    // 단방향: Menu(N) -> Store(1)
    // store 반영 후 다시 수정
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "store_id", nullable = false)
//    private Store store;
    @Column(name = "store_id", nullable = false)
    private UUID storeId;

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

    // 양방향: OptionGroup(N) <-> Menu(1)
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionGroup> optionGroupList = new ArrayList<>();

    // store 반영 후 다시 수정
    @Builder
    public Menu(MenuCategory menuCategory, UUID storeId, String name, int price,
                String description, boolean isDescriptionAiGenerated, int displayOrder) {
        this.menuCategory = menuCategory;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.isDescriptionAiGenerated = isDescriptionAiGenerated;
        this.displayOrder = displayOrder;
    }

    public void addOptionGroup(OptionGroup optionGroup) {
        this.optionGroupList.add(optionGroup);
        optionGroup.assignMenu(this);
    }
}
