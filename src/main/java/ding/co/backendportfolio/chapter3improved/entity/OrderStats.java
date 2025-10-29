package ding.co.backendportfolio.chapter3improved.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//즉 특정 작업을 특정시간에 주기적으로 자동으로 구성하는 프로세스를 수행하는것이 batch 작업이라 한다.
//예를들어 새벽 3시마다 대규모 정산 작업을 자동 수행 하는것처럼.

@Entity
@Table(name = "ch3_order_stats")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStats {
    @Id
    private Long memberId;
    
    private String email;
    private int orderCount;
    private long totalAmount;
    private double avgAmount;
    private LocalDateTime lastOrderDate;
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
} 