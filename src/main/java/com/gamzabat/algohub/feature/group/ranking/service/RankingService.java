package com.gamzabat.algohub.feature.group.ranking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.group.ranking.exception.CannotFoundRankingException;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
	private final RankingRepository rankingRepository;
	private final StudyGroupRepository groupRepository;
	private final GroupMemberRepository groupMemberRepository;

	public static final double SCORE_SCALING_FACTOR = 1e-6;

	@Transactional(readOnly = true)
	public Page<GetRankingResponse> getAllRank(User user, Long groupId, Pageable pageable) {

		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "랭킹을 확인할 권한이 없습니다.");
		}

		return rankingRepository.findAllByStudyGroup(group, pageable)
			.map(GetRankingResponse::toDTO);
	}

	@Transactional
	public void updateScore(GroupMember member, LocalDate problemEndDate,
		LocalDateTime solvedDateTime) {
		Ranking ranking = rankingRepository.findByMember(member)
			.orElseThrow(() -> new CannotFoundRankingException("유저의 랭킹 정보를 조회할 수 없습니다."));

		ranking.increaseSolvedCount();
		ranking.increaseScore(calculateNewScore(solvedDateTime));
		log.info("success to update ranking score");
	}

	private double calculateNewScore(LocalDateTime solvedDateTime) {
		return solvedDateTime.atZone(java.time.ZoneId.of("Asia/Seoul")).toEpochSecond() * SCORE_SCALING_FACTOR;
	}
}
