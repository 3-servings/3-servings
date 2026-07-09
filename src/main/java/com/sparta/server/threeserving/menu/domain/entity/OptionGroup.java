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
@Table(name = "p_option_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE p_option_group SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class OptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 양방향: OptionGroup(N) <-> Menu(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_multiple", nullable = false)
    private boolean isMultiple = false;

    // 양방향: OptionItem(N) <-> OptionGroup(1)
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItemList = new ArrayList<>();

    @Builder
    public OptionGroup(Menu menu, String name, boolean isMultiple) {
        this.menu = menu;
        this.name = name;
        this.isMultiple = isMultiple;
    }

    public void assignMenu(Menu menu) {
        this.menu = menu;
    }

    public void addOptionItem(OptionItem optionItem) {
        this.optionItemList.add(optionItem);
        optionItem.assignOptionGroup(this);
    }
}
