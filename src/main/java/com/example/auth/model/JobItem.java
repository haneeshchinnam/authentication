package com.example.auth.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_jobs_items_file_path", columnNames = "file_path")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private PrintJob job;

    @Builder.Default
    private boolean deleted = false;

    @UpdateTimestamp
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "print_type", nullable = false, length = 50)
    private String printType;

    @Column(name = "unit_price_cents", nullable = false)
    private Integer unitPriceCents;

    @Column(name = "page_count", nullable = false)
    @Builder.Default
    private Integer pageCount = 1;
}
