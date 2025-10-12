package com.gamzabat.algohub.feature.recommendation.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gamzabat.algohub.feature.recommendation.service.RecommendationBatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationScheduler {

	private final RecommendationBatchService recommendationBatchService;

	/**
	 * 매주 월요일 오전 4시 ->  '이번주 가장 많이 활동한 스터디' 집계
	 * cron = "초 분 시 일 월 요일"
	 */
	@Scheduled(cron = "0 0 4 * * MON")
	public void scheduleMostActiveStudy() {
		recommendationBatchService.calculateAndSaveMostActiveStudy();
	}

	/**
	 * 매일 오전 4시 10분 -> '최근 가입률이 높은 스터디' 집계
	 */
	@Scheduled(cron = "0 10 4 * * *")
	public void scheduleHighJoinRateStudy() {
		recommendationBatchService.calculateAndSaveHighJoinRateStudy();
	}

	/**
	 * 매일 오전 4시 20분 -> '사용자 및 스터디 난이도'를 집계
	 */
	@Scheduled(cron = "0 20 4 * * *")
	public void scheduleDifficultyInfo() {
		recommendationBatchService.calculateAndSaveDifficultyInfo();
	}
}