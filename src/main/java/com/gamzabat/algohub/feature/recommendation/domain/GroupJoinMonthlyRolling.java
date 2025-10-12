package com.gamzabat.algohub.feature.recommendation.domain;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_join_monthly_rolling")
@Getter
@NoArgsConstructor
public class GroupJoinMonthlyRolling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id", nullable = false)
    private StudyGroup studyGroup;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "new_members", nullable = false)
    private Integer newMembers;

    @Column(name = "members_before_window", nullable = false)
    private Integer membersBeforeWindow;

    @Column(name = "join_rate", nullable = false)
    private Double joinRate;
}
