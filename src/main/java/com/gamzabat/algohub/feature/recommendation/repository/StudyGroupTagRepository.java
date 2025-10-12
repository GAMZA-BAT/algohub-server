package com.gamzabat.algohub.feature.recommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.recommendation.domain.StudyGroupTag;
import com.gamzabat.algohub.feature.recommendation.domain.TagType;

public interface StudyGroupTagRepository extends JpaRepository<StudyGroupTag, Long> {
    Optional<StudyGroupTag> findTop1ByTagTypeOrderByScoreDescFirstAchievedAtAsc(TagType tagType);
}
