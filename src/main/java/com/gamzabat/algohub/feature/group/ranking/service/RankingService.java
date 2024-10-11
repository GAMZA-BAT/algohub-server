package com.gamzabat.algohub.feature.group.ranking.service;

import java.util.Comparator;
import java.util.List;

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

	@Transactional(readOnly = true)
	public List<GetRankingResponse> getTopRank(User user, Long groupId) {

		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "랭킹을 확인할 권한이 없습니다.");
		}

		List<Ranking> ranking = rankingRepository.findAllByStudyGroup(group)
			.stream()
			.filter(r -> r.getSolvedCount() != 0)
			.sorted(Comparator.comparing(Ranking::getCurrentRank))
			.toList();

		if (ranking.size() >= 3)
			ranking.subList(0, 3);

		return getRankingResponse(ranking);
	}

	@Transactional(readOnly = true)
	public List<GetRankingResponse> getAllRank(User user, Long groupId) {

		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "랭킹을 확인할 권한이 없습니다.");
		}

		List<Ranking> ranking = rankingRepository.findAllByStudyGroup(group)
			.stream()
			.sorted(Comparator.comparing(Ranking::getCurrentRank))
			.toList();
		return getRankingResponse(ranking);
	}

	private List<GetRankingResponse> getRankingResponse(List<Ranking> ranking) {
		return ranking.stream().map(r -> new GetRankingResponse(
				r.getMember().getUser().getNickname(),
				r.getMember().getUser().getProfileImage(),
				r.getCurrentRank(),
				(long)r.getSolvedCount(),
				r.getRankDiff()))
			.toList();
	}

	public void updateRanking(GroupMember member) {
		Ranking ranking = rankingRepository.findByMember(member)
			.orElseThrow(() -> new CannotFoundRankingException("유저의 랭킹 정보를 조회할 수 없습니다."));
		ranking.increaseSolvedCount();

		List<Ranking> rankings = rankingRepository.findAllByStudyGroup(member.getStudyGroup());
		rankings.sort((r1, r2) -> {
			int compare = r2.getSolvedCount() - r1.getSolvedCount();
			if (compare == 0)
				return r1.getMember().getJoinDate().compareTo(r2.getMember().getJoinDate());
			return r2.getSolvedCount() - r1.getSolvedCount();
		});
		for (Ranking r : rankings) {
			int originRank = r.getCurrentRank();
			int newRank = rankings.indexOf(r) + 1;
			r.updateRank(newRank);
			r.updateRankDiff(generateRankDiffString(originRank, newRank));
		}
	}

	private String generateRankDiffString(int originRank, int newRank) {
		int rankDiff = originRank - newRank;
		if (rankDiff > 0)
			return "+" + rankDiff;
		else if (rankDiff < 0)
			return String.valueOf(rankDiff);
		else
			return "-";
	}
}
