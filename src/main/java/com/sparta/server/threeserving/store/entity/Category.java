package com.sparta.server.threeserving.store.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Table(name = "p_category")
@Entity
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Category(String name, Long createdBy){
        this.name = name;
        this.createdBy = createdBy;
    }


    public void changeIsActive(Boolean isActive, Long updatedBy){
        this.isActive = isActive;
        this.updatedBy = updatedBy;
        this.updatedAt = java.time.LocalDateTime.now();
    }


}
