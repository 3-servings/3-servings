package com.sparta.server.threeserving.menu.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_option_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE p_option_item SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OptionItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 양방향: OptionItem(N) <-> OptionGroup(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    private OptionGroup optionGroup;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuStatus status = MenuStatus.AVAILABLE;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Builder
    public OptionItem(OptionGroup optionGroup, String name, int price, int displayOrder) {
        this.optionGroup = optionGroup;
        this.name = name;
        this.price = price;
        this.displayOrder = displayOrder;
    }

    public void assignOptionGroup(OptionGroup optionGroup) {
        this.optionGroup = optionGroup;
    }

    public void updateStatus(MenuStatus newStatus) {
        this.status = newStatus;
    }
}
