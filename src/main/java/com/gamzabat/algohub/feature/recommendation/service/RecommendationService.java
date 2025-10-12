package com.gamzabat.algohub.feature.recommendation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.recommendation.domain.GroupDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.domain.TagType;
import com.gamzabat.algohub.feature.recommendation.domain.UserDifficultyMonthlyRolling;
import com.gamzabat.algohub.feature.recommendation.dto.HomeRecommendationsResponse;
import com.gamzabat.algohub.feature.recommendation.dto.RecommendationItemDto;
import com.gamzabat.algohub.feature.recommendation.dto.StudyGroupSummaryDto;
import com.gamzabat.algohub.feature.recommendation.repository.GroupDifficultyMonthlyRollingRepository;
import com.gamzabat.algohub.feature.recommendation.repository.StudyGroupTagRepository;
import com.gamzabat.algohub.feature.recommendation.repository.UserDifficultyMonthlyRollingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final StudyGroupTagRepository studyGroupTagRepository;
	private final UserDifficultyMonthlyRollingRepository userDifficultyMonthlyRollingRepository;
	private final GroupDifficultyMonthlyRollingRepository groupDifficultyMonthlyRollingRepository;
	private final StudyGroupRepository studyGroupRepository;

	public HomeRecommendationsResponse getHomeRecommendations(Long userId) {
		RecommendationItemDto mostActive = buildFromTagSnapshot(TagType.MOST_ACTIVE_THIS_WEEK)
			.orElseGet(() -> fallbackFromRecentGroup(TagType.MOST_ACTIVE_THIS_WEEK));

		RecommendationItemDto highJoin = buildFromTagSnapshot(TagType.HIGH_JOIN_RATE_RECENT)
			.orElseGet(() -> fallbackFromRecentGroup(TagType.HIGH_JOIN_RATE_RECENT));

		RecommendationItemDto similar = buildSimilarDifficulty(userId)
			.orElseGet(() -> fallbackFromRecentGroup(TagType.SIMILAR_DIFFICULTY));

		return new HomeRecommendationsResponse(mostActive, highJoin, similar);
	}

	private Optional<RecommendationItemDto> buildFromTagSnapshot(TagType tagType) {
		return studyGroupTagRepository.findTop1ByTagTypeOrderByScoreDescFirstAchievedAtAsc(tagType)
			.map(tag -> new RecommendationItemDto(
				tagType,
				tag.getScore(),
				toSummary(tag.getStudyGroup(), List.of(tagType.name()))
			));
	}

	private Optional<RecommendationItemDto> buildSimilarDifficulty(Long userId) {
		if (userId == null)
			return Optional.empty();

		Optional<UserDifficultyMonthlyRolling> userRollingOpt =
			userDifficultyMonthlyRollingRepository.findTop1ByUserIdOrderByWindowEndDesc(userId);

		if (userRollingOpt.isEmpty())
			return Optional.empty();

		UserDifficultyMonthlyRolling userRolling = userRollingOpt.get();

		Optional<GroupDifficultyMonthlyRolling> groupOpt =
			groupDifficultyMonthlyRollingRepository.findTopSimilarByWindow(
				userRolling.getWindowStart(), userRolling.getWindowEnd(), userRolling.getAvgDifficulty());

		return groupOpt.map(g -> {
			double diff = Math.abs(g.getAvgDifficulty() - userRolling.getAvgDifficulty());
			StudyGroup group = g.getStudyGroup();
			return new RecommendationItemDto(
				TagType.SIMILAR_DIFFICULTY,
				diff,
				toSummary(group, List.of(TagType.SIMILAR_DIFFICULTY.name()))
			);
		});
	}

	private RecommendationItemDto fallbackFromRecentGroup(TagType tagType) {
		StudyGroup group = studyGroupRepository.findAll().stream()
			.sorted((a, b) -> Long.compare(b.getId(), a.getId()))
			.findFirst()
			.orElse(null);

		if (group == null) {
			return null;
		}

		return new RecommendationItemDto(tagType, 0.0, toSummary(group, List.of(tagType.name())));
	}

	private StudyGroupSummaryDto toSummary(StudyGroup group, List<String> tags) {
		return new StudyGroupSummaryDto(
			group.getId(),
			group.getName(),
			group.getIntroduction(),
			group.getGroupImage(),
			tags
		);
	}
}
