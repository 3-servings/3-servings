package com.sparta.server.threeserving.image.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_image")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // ✨ Auditing 기능 활성화
@SQLDelete(sql = "UPDATE p_image SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, columnDefinition = "uuid")
    private UUID id;

    // "MENU", "REVIEW", "STORE" 등 대상을 구분
    @Column(name = "domain_type", nullable = false, length = 20)
    private String domainType;

    // 연결될 도메인의 id
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private int sequence = 1;

    @Column(name = "origin_name", nullable = false, length = 255)
    private String originName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(name = "image_path", nullable = false, length = 500)
    private String imagePath;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "file_size", nullable = false)
    private long fileSize = 0;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public Image(String domainType, UUID targetId, int sequence, String originName, String storedName, String imagePath, String imageUrl, long fileSize, String contentType) {
        this.domainType = domainType;
        this.targetId = targetId;
        this.sequence = sequence;
        this.originName = originName;
        this.storedName = storedName;
        this.imagePath = imagePath;
        this.imageUrl = imageUrl;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }
}
