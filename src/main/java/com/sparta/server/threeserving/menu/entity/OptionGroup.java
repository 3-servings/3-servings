package com.sparta.server.threeserving.menu.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_option_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class OptionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 단방향: OptionGroup(N) <-> Store(1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "min_select", nullable = false)
    private int minSelect = 0;

    @Column(name = "max_select", nullable = false)
    private int maxSelect = 1;

    // 양방향: OptionGroup(1) <-> OptionItem(N)
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItemList = new ArrayList<>();

    @Builder
    public OptionGroup(Store store, String name, int minSelect, int maxSelect) {
        this.store = store;
        this.name = name;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
    }

    public void addOptionItem(OptionItem optionItem) {
        this.optionItemList.add(optionItem);
        optionItem.assignOptionGroup(this);
    }

    public void update(String name, int minSelect, int maxSelect) {
        this.name = name;
        this.minSelect = minSelect;
        this.maxSelect = maxSelect;
    }

    @Override
    public void softDelete(Long deletedBy) {
        super.softDelete(deletedBy);

        for (OptionItem optionItem : optionItemList) {
            optionItem.softDelete(deletedBy);
        }
    }
}
