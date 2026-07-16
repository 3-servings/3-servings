package com.sparta.server.threeserving.order_management.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "p_reject_reason_code")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RejectReasonCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    public RejectReasonCode(String code, String description) {
        this.code = code;
        this.description = description;
    }


}
