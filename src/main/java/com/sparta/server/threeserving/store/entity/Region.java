package com.sparta.server.threeserving.store.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Table(name = "p_region")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Region extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder.Default
    @Column(name = "is_service_arrea", nullable = false)
    private Boolean isServiceArea = false;

    public Region(String name){
        this.name = name;
    }

    public void update(String name){
        this.name = name;
    }

    public void changeServiceArea(Boolean isServiceArea){
        this.isServiceArea = isServiceArea;
    }
}
