package com.traassist.tra_assist.observability;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "query_logs")
@Data
public class QueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String query;

    @Column(columnDefinition = "text")
    private String answer;

    @Column
    private String confidence;

    @Column
    private double topSimilarity;

    @Column
    private boolean swahili;

    @Column
    private long responseTimeMs;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
