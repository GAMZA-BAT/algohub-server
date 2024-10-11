package com.gamzabat.algohub.feature.studygroup.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
import com.gamzabat.algohub.feature.studygroup.domain.BookmarkedStudyGroup;
import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.domain.Ranking;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.studygroup.dto.CheckSolvedProblemResponse;
import com.gamzabat.algohub.feature.studygroup.dto.CreateGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.CreateGroupResponse;
import com.gamzabat.algohub.feature.studygroup.dto.EditGroupRequest;
import com.gamzabat.algohub.feature.studygroup.dto.GetGroupMemberResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetGroupResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetRankingResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupListsResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupResponse;
import com.gamzabat.algohub.feature.studygroup.dto.GetStudyGroupWithCodeResponse;
import com.gamzabat.algohub.feature.studygroup.dto.UpdateGroupMemberRoleRequest;
import com.gamzabat.algohub.feature.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundProblemException;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundRankingException;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.studygroup.repository.BookmarkedStudyGroupRepository;
import com.gamzabat.algohub.feature.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.studygroup.repository.RankingRepository;
import com.gamzabat.algohub.feature.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyGroupService {
	private final StudyGroupRepository groupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final ImageService imageService;
	private final SolutionRepository solutionRepository;
	private final ProblemRepository problemRepository;
	private final UserRepository userRepository;
	private final StudyGroupRepository studyGroupRepository;
	private final BookmarkedStudyGroupRepository bookmarkedStudyGroupRepository;
	private final RankingRepository rankingRepository;

	@Transactional
	public CreateGroupResponse createGroup(User user, CreateGroupRequest request, MultipartFile profileImage) {
		String imageUrl = imageService.saveImage(profileImage);
		String inviteCode = NanoIdUtils.randomNanoId();

		StudyGroup group = StudyGroup.builder()
			.name(request.name())
			.startDate(request.startDate())
			.endDate(request.endDate())
			.introduction(request.introduction())
			.groupImage(imageUrl)
			.groupCode(inviteCode)
			.build();
		groupRepository.save(group);

		GroupMember member = GroupMember.builder()
			.studyGroup(group)
			.user(user)
			.role(RoleOfGroupMember.OWNER)
			.joinDate(LocalDate.now())
			.build();
		groupMemberRepository.save(member);

		rankingRepository.save(Ranking.builder()
			.member(member)
			.solvedCount(0)
			.currentRank(1)
			.rankDiff("-")
			.build());
		log.info("success to save study group");
		return new CreateGroupResponse(inviteCode);
	}

	@Transactional
	public void joinGroupWithCode(User user, String code) {
		StudyGroup studyGroup = groupRepository.findByGroupCode(code)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		if (groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여한 그룹 입니다.");

		groupMemberRepository.save(
			GroupMember.builder()
				.studyGroup(studyGroup)
				.user(user)
				.role(RoleOfGroupMember.PARTICIPANT)
				.joinDate(LocalDate.now())
				.build()
		);
		log.info("success to join study group");
	}

	@Transactional
	public void deleteGroup(User user, Long groupId) {
		StudyGroup studyGroup = groupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isOwner(groupMember)) { // owner
			bookmarkedStudyGroupRepository.deleteAll(bookmarkedStudyGroupRepository.findAllByStudyGroup(studyGroup));
			groupMemberRepository.delete(groupMember);
			groupRepository.delete(studyGroup);
		} else { // member
			deleteMemberFromStudyGroup(user, studyGroup, groupMember);
		}
		log.info("success to delete(exit) study group");
	}

	@Transactional
	public void deleteMember(User user, Long userId, Long groupId) {
		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));
		GroupMember owner = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(),
					"멤버 삭제 권한이 없습니다. : 참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isOwner(owner)) {
			User targetUser = userRepository.findById(userId)
				.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 유저입니다."));

			GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(targetUser, group)
				.orElseThrow(
					() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여하지 않은 회원입니다."));

			deleteMemberFromStudyGroup(user, group, groupMember);
		} else {
			throw new UserValidationException("멤버를 삭제 할 권한이 없습니다.");
		}
	}

	private void deleteMemberFromStudyGroup(User user, StudyGroup studyGroup, GroupMember groupMember) {
		bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user, studyGroup)
			.ifPresent(bookmarkedStudyGroupRepository::delete);
		rankingRepository.deleteByMember(groupMember);
		groupMemberRepository.delete(groupMember);
	}

	@Transactional(readOnly = true)
	public GetStudyGroupListsResponse getStudyGroupList(User user) {
		List<StudyGroup> groups = groupRepository.findAllByUser(user);

		List<GetStudyGroupResponse> bookmarked = bookmarkedStudyGroupRepository.findAllByUser(user).stream()
			.map(bookmark -> GetStudyGroupResponse.toDTO(bookmark.getStudyGroup(), user, true,
				getStudyGroupOwner(bookmark.getStudyGroup())))
			.toList();

		LocalDate today = LocalDate.now();

		List<GetStudyGroupResponse> done = groups.stream()
			.filter(group -> group.getEndDate() != null && group.getEndDate().isBefore(today))
			.map(
				group -> GetStudyGroupResponse.toDTO(group, user, isBookmarked(user, group), getStudyGroupOwner(group)))
			.toList();

		List<GetStudyGroupResponse> inProgress = groups.stream()
			.filter(
				group -> !(group.getStartDate() == null || group.getStartDate().isAfter(today))
					&& !(group.getEndDate() == null || group.getEndDate().isBefore(today)))
			.map(
				group -> GetStudyGroupResponse.toDTO(group, user, isBookmarked(user, group), getStudyGroupOwner(group)))
			.toList();

		List<GetStudyGroupResponse> queued = groups.stream()
			.filter(group -> group.getStartDate() != null && group.getStartDate().isAfter(today))
			.map(
				group -> GetStudyGroupResponse.toDTO(group, user, isBookmarked(user, group), getStudyGroupOwner(group)))
			.toList();

		GetStudyGroupListsResponse response = new GetStudyGroupListsResponse(bookmarked, done, inProgress, queued);

		log.info("success to get study group list");
		return response;
	}

	private User getStudyGroupOwner(StudyGroup group) {
		return groupMemberRepository.findByStudyGroupAndRole(group, RoleOfGroupMember.OWNER).getUser();
	}

	@Transactional
	public void editGroup(User user, EditGroupRequest request, MultipartFile groupImage) {
		StudyGroup group = groupRepository.findById(request.id())
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		if (!RoleOfGroupMember.isOwner(groupMember))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "그룹 정보 수정에 대한 권한이 없습니다.");

		if (groupImage != null) {
			if (group.getGroupImage() != null)
				imageService.deleteImage(group.getGroupImage());
			String imageUrl = imageService.saveImage(groupImage);
			group.editGroupImage(imageUrl);
		}
		group.editGroupInfo(
			request.name(),
			request.startDate(),
			request.endDate(),
			request.introduction()
		);
		log.info("success to edit group info");
	}

	@Transactional(readOnly = true)
	public List<GetGroupMemberResponse> getGroupMemberList(User user, Long id) {
		StudyGroup group = groupRepository.findById(id)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, group))
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "그룹 정보를 확인할 권한이 없습니다");

		List<GroupMember> groupMembers = groupMemberRepository.findAllByStudyGroup(group);

		List<GetGroupMemberResponse> responseList = new ArrayList<>();

		for (GroupMember groupMember : groupMembers) {
			String nickname = groupMember.getUser().getNickname();
			LocalDate joinDate = groupMember.getJoinDate();

			Long correctSolution = solutionRepository.countDistinctCorrectSolutionsByUserAndGroup(
				groupMember.getUser(), id, BOJResultConstants.CORRECT);
			Long problems = problemRepository.countProblemsByGroupId(id);
			String achivement;
			if (correctSolution == 0) {
				achivement = "0%";
			} else {
				achivement = getPercentage(correctSolution, problems) + "%";
			}

			RoleOfGroupMember role = groupMember.getRole();
			String profileImage = groupMember.getUser().getProfileImage();
			Long userId = groupMember.getUser().getId();
			responseList.add(
				new GetGroupMemberResponse(nickname, joinDate, achivement, role, profileImage, userId));
		}
		responseList.sort(Comparator.comparing(GetGroupMemberResponse::getRole));

		return responseList;
	}

	@Transactional(readOnly = true)
	public List<CheckSolvedProblemResponse> getCheckingSolvedProblem(User user, Long problemId) {
		Problem problem = problemRepository.findById(problemId)
			.orElseThrow(() -> new CannotFoundProblemException("문제를 찾을 수 없습니다."));
		StudyGroup studyGroup = problem.getStudyGroup();

		if (groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup)) {
			List<GroupMember> groupMembers = groupMemberRepository.findAllByStudyGroup(studyGroup);

			List<CheckSolvedProblemResponse> responseList = new ArrayList<>();

			for (GroupMember groupMember : groupMembers) {
				String profileImage = groupMember.getUser().getProfileImage();
				Long groupMemberId = groupMember.getUser().getId();
				String nickname = groupMember.getUser().getNickname();
				Boolean solved = solutionRepository.existsByUserAndProblem(groupMember.getUser(), problem);
				responseList.add(new CheckSolvedProblemResponse(groupMemberId, profileImage, nickname, solved));
			}
			return responseList;
		} else {
			throw new UserValidationException("풀이 여부 목록을 확인할 권한이 없습니다.");
		}
	}

	@Transactional(readOnly = true)
	public String getGroupCode(User user, Long groupId) {
		StudyGroup studyGroup = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾지 못했습니다."));
		GroupMember owner = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isOwner(owner))
			return studyGroup.getGroupCode();
		else
			throw new UserValidationException("초대 코드를 조회할 권한이 없습니다.");
	}

	@Transactional(readOnly = true)
	public GetStudyGroupWithCodeResponse getGroupByCode(String code) {
		StudyGroup group = groupRepository.findByGroupCode(code)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));
		return GetStudyGroupWithCodeResponse.toDTO(group);
	}

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

	@Transactional(readOnly = true)
	public GetGroupResponse getGroup(User user, Long groupId) {

		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		GetGroupResponse response = new GetGroupResponse(group.getId(), group.getName(), group.getStartDate(),
			group.getEndDate(), group.getIntroduction(), group.getGroupImage(), RoleOfGroupMember.isOwner(member),
			getStudyGroupOwner(group).getNickname());
		return response;
	}

	private String getPercentage(Long numerator, Long denominator) {
		if (denominator == 0) {
			throw new ArithmeticException("Division by zero");
		}

		BigDecimal num = new BigDecimal(numerator);
		BigDecimal den = new BigDecimal(denominator);
		BigDecimal percentage = num.multiply(BigDecimal.valueOf(100)).divide(den, 0, RoundingMode.HALF_UP);

		return percentage.toString();
	}

	@Transactional
	public String updateBookmarkGroup(User user, Long groupId) {
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹 입니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다.");

		Optional<BookmarkedStudyGroup> bookmarked = bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user,
			studyGroup);

		if (bookmarked.isEmpty()) {
			bookmarkedStudyGroupRepository.save(
				BookmarkedStudyGroup.builder().studyGroup(studyGroup).user(user).build());
			return "스터디 그룹 즐겨찾기 추가 성공";
		} else {
			bookmarkedStudyGroupRepository.delete(bookmarked.get());
			return "스터디 그룹 즐겨찾기 삭제 성공";
		}
	}

	private boolean isBookmarked(User user, StudyGroup group) {
		return bookmarkedStudyGroupRepository.existsByUserAndStudyGroup(user, group);
	}

	@Transactional
	public void updateGroupMemberRole(User user, UpdateGroupMemberRoleRequest request) {
		StudyGroup group = studyGroupRepository.findById(request.studyGroupId())
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));

		GroupMember owner = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		if (!RoleOfGroupMember.isOwner(owner))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "스터디 그룹의 멤버 역할을 수정할 권한이 없습니다.");

		User targetUser = userRepository.findById(request.memberId())
			.orElseThrow(() -> new UserValidationException("존재하지 않는 회원입니다."));

		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(targetUser, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "해당 스터디 그룹에 참여하지 않은 회원입니다."));

		member.updateRole(RoleOfGroupMember.fromValue(request.role()));
		log.info("success to update group member role");
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
