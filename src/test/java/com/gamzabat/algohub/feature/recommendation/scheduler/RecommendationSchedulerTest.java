package com.gamzabat.algohub.feature.recommendation.scheduler;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.gamzabat.algohub.feature.recommendation.service.RecommendationBatchService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationSchedulerTest {

	@Mock
	private RecommendationBatchService recommendationBatchService;

	@InjectMocks
	private RecommendationScheduler recommendationScheduler;

	@Test
	@DisplayName("이번주 가장 많이 활동한 스터디 스케줄링 성공")
	void scheduleMostActiveStudy_Success() {
		// given & when
		recommendationScheduler.scheduleMostActiveStudy();

		// then
		verify(recommendationBatchService).calculateAndSaveMostActiveStudy();
	}

	@Test
	@DisplayName("최근 가입률이 높은 스터디 스케줄링 성공")
	void scheduleHighJoinRateStudy_Success() {
		// given & when
		recommendationScheduler.scheduleHighJoinRateStudy();

		// then
		verify(recommendationBatchService).calculateAndSaveHighJoinRateStudy();
	}

	@Test
	@DisplayName("난이도 정보 스케줄링 성공")
	void scheduleDifficultyInfo_Success() {
		// given & when
		recommendationScheduler.scheduleDifficultyInfo();

		// then
		verify(recommendationBatchService).calculateAndSaveDifficultyInfo();
	}
}
