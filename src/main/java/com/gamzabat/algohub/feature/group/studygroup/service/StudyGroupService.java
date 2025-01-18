package com.gamzabat.algohub.feature.group.studygroup.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.gamzabat.algohub.constants.BOJResultConstants;
import com.gamzabat.algohub.enums.ImageType;
import com.gamzabat.algohub.exception.StudyGroupValidationException;
import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.group.ranking.domain.Ranking;
import com.gamzabat.algohub.feature.group.ranking.repository.RankingRepository;
import com.gamzabat.algohub.feature.group.studygroup.domain.BookmarkedStudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.group.studygroup.dto.BookmarkStatus;
import com.gamzabat.algohub.feature.group.studygroup.dto.CheckSolvedProblemResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.CreateGroupRequest;
import com.gamzabat.algohub.feature.group.studygroup.dto.EditGroupRequest;
import com.gamzabat.algohub.feature.group.studygroup.dto.EditGroupVisibilityRequest;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetGroupMemberResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetGroupResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetGroupSettingResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetStudyGroupListsResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetStudyGroupResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GetStudyGroupWithCodeResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GroupCodeResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.GroupRoleResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.UpdateBookmarkResponse;
import com.gamzabat.algohub.feature.group.studygroup.dto.UpdateGroupMemberRoleRequest;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundProblemException;
import com.gamzabat.algohub.feature.group.studygroup.exception.CannotFoundUserException;
import com.gamzabat.algohub.feature.group.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.group.studygroup.repository.BookmarkedStudyGroupRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.GroupMemberRepository;
import com.gamzabat.algohub.feature.group.studygroup.repository.StudyGroupRepository;
import com.gamzabat.algohub.feature.image.service.ImageService;
import com.gamzabat.algohub.feature.notice.repository.NoticeCommentRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeReadRepository;
import com.gamzabat.algohub.feature.notice.repository.NoticeRepository;
import com.gamzabat.algohub.feature.notification.domain.NotificationSetting;
import com.gamzabat.algohub.feature.notification.enums.NotificationCategory;
import com.gamzabat.algohub.feature.notification.repository.NotificationSettingRepository;
import com.gamzabat.algohub.feature.notification.service.NotificationService;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.problem.repository.ProblemRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionCommentRepository;
import com.gamzabat.algohub.feature.solution.repository.SolutionRepository;
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
	private final NoticeRepository noticeRepository;
	private final NoticeCommentRepository noticeCommentRepository;
	private final SolutionCommentRepository solutionCommentRepository;
	private final NoticeReadRepository noticeReadRepository;
	private final ObjectProvider<StudyGroupService> studyGroupServiceProvider;
	private final NotificationSettingRepository notificationSettingRepository;
	private final NotificationService notificationService;

	@Transactional
	public GroupCodeResponse createGroup(User user, CreateGroupRequest request, MultipartFile profileImage) {
		String inviteCode = NanoIdUtils.randomNanoId();

		StudyGroup group = groupRepository.save(StudyGroup.builder()
			.name(request.name())
			.startDate(request.startDate())
			.endDate(request.endDate())
			.introduction(request.introduction())
			.groupCode(inviteCode)
			.build());

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

		notificationSettingRepository.save(
			NotificationSetting.builder().member(member).build()
		);

		saveGroupImage(profileImage, group);
		log.info("success to save study group");
		return new GroupCodeResponse(inviteCode);
	}

	@Transactional
	public void joinGroupWithCode(User user, String code) {
		StudyGroup studyGroup = groupRepository.findByGroupCode(code)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		if (groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "이미 참여한 그룹 입니다.");

		GroupMember member = GroupMember.builder()
			.studyGroup(studyGroup)
			.user(user)
			.role(RoleOfGroupMember.PARTICIPANT)
			.joinDate(LocalDate.now())
			.build();
		groupMemberRepository.save(member);

		notificationSettingRepository.save(
			NotificationSetting.builder().member(member).build()
		);

		rankingRepository.save(Ranking.builder()
			.member(member)
			.currentRank(groupMemberRepository.countByStudyGroup(studyGroup))
			.solvedCount(0)
			.rankDiff("-")
			.build()
		);

		sendNewMemberNotification(studyGroup, member);

		log.info("success to join study group");
	}

	@Transactional
	public void deleteGroup(User user, Long groupId) {
		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));

		GroupMember owner = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹입니다."));

		if (!RoleOfGroupMember.isOwner(owner)) {
			throw new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "스터디 그룹 삭제는 방장만 가능합니다.");
		}

		deleteAllAboutGroup(group);

		log.info("success to delete study group");
	}

	private void deleteAllAboutGroup(StudyGroup group) {
		bookmarkedStudyGroupRepository.deleteAllByStudyGroup(group);
		rankingRepository.deleteAllByStudyGroup(group);
		notificationSettingRepository.deleteAllByStudyGroup(group);
		noticeCommentRepository.deleteAllByStudyGroup(group);
		noticeReadRepository.deleteAllByStudyGroup(group);
		noticeRepository.deleteAllByStudyGroup(group);
		solutionCommentRepository.deleteAllByStudyGroup(group);
		solutionRepository.deleteAllByStudyGroup(group);
		problemRepository.deleteAllByStudyGroup(group);
		groupMemberRepository.deleteAllByStudyGroup(group);
		groupRepository.delete(group);
	}

	@Transactional
	public void exitGroup(User user, Long groupId) {
		StudyGroup studyGroup = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));

		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹입니다."));

		studyGroupServiceProvider.getObject().deleteMemberFromStudyGroup(user, groupMember, studyGroup);

		if (RoleOfGroupMember.isOwner(groupMember)) {
			List<GroupMember> members = groupMemberRepository.findAllByStudyGroup(studyGroup)
				.stream()
				.sorted(Comparator.comparing(GroupMember::getRole).thenComparing(GroupMember::getJoinDate))
				.toList();

			if (members.isEmpty()) {
				deleteAllAboutGroup(studyGroup);
			} else {
				members.getFirst().updateRole(RoleOfGroupMember.OWNER);
			}
		}

		log.info("success to exit study group");
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

			studyGroupServiceProvider.getObject().deleteMemberFromStudyGroup(user, groupMember, group);
		} else {
			throw new UserValidationException("멤버를 삭제 할 권한이 없습니다.");
		}
	}

	public void deleteMemberFromStudyGroup(User user, GroupMember groupMember, StudyGroup studyGroup) {
		bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user, studyGroup)
			.ifPresent(bookmarkedStudyGroupRepository::delete);
		rankingRepository.deleteByMember(groupMember);
		notificationSettingRepository.deleteByMember(groupMember);
		solutionRepository.deleteAllByStudyGroupAndUser(studyGroup, user);
		noticeReadRepository.deleteAllByStudyGroupAndUser(studyGroup, user);
		groupMemberRepository.delete(groupMember);
		log.info("success to delete group member");
	}

	@Transactional(readOnly = true)
	public GetStudyGroupListsResponse getStudyGroupList(User user) {
		List<StudyGroup> groups = groupRepository.findAllByUser(user);
		LocalDate today = LocalDate.now();

		List<GetStudyGroupResponse> bookmarked = groups.stream()
			.filter(group -> isBookmarked(user, group))
			.map(group -> getStudyGroupResponseDTO(user, group))
			.toList();

		List<GetStudyGroupResponse> done = groups.stream()
			.filter(group -> isDone(today, group))
			.map(group -> getStudyGroupResponseDTO(user, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		List<GetStudyGroupResponse> inProgress = groups.stream()
			.filter(
				group -> isInProgress(today, group))
			.map(group -> getStudyGroupResponseDTO(user, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		List<GetStudyGroupResponse> queued = groups.stream()
			.filter(group -> isQueued(today, group))
			.map(group -> getStudyGroupResponseDTO(user, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		GetStudyGroupListsResponse response = new GetStudyGroupListsResponse(bookmarked, done, inProgress, queued);

		log.info("success to get my study group list");
		return response;
	}

	private static boolean isDone(LocalDate today, StudyGroup group) {
		return group.getEndDate() != null && group.getEndDate().isBefore(today);
	}

	private static boolean isInProgress(LocalDate today, StudyGroup group) {
		return !(group.getStartDate() == null || group.getStartDate().isAfter(today))
			&& !(group.getEndDate() == null || group.getEndDate().isBefore(today));
	}

	private static boolean isQueued(LocalDate today, StudyGroup group) {
		return group.getStartDate() != null && group.getStartDate().isAfter(today);
	}

	private GetStudyGroupResponse getStudyGroupResponseDTO(User user, StudyGroup group) {
		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(() -> new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹입니다."));
		return GetStudyGroupResponse.toDTO(group, member, isBookmarked(user, group), getStudyGroupOwner(group),
			member.getIsVisible());
	}

	private User getStudyGroupOwner(StudyGroup group) {
		return groupMemberRepository.findByStudyGroupAndRole(group, RoleOfGroupMember.OWNER).getUser();
	}

	@Transactional
	public void editGroup(User user, Long groupId, EditGroupRequest request, MultipartFile groupImage) {
		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new StudyGroupValidationException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 그룹 입니다."));

		GroupMember groupMember = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		if (!RoleOfGroupMember.isOwner(groupMember))
			throw new StudyGroupValidationException(HttpStatus.FORBIDDEN.value(), "그룹 정보 수정에 대한 권한이 없습니다.");

		editGroupImage(groupImage, group);
		group.editGroupInfo(
			request.name(),
			request.startDate(),
			request.endDate(),
			request.introduction()
		);
		log.info("success to edit group info");
	}

	private void editGroupImage(MultipartFile inputImage, StudyGroup group) {
		if (inputImage == null || inputImage.isEmpty()) {
			handleNullInputImage(group);
			return;
		}

		if (group.getGroupImage() != null) {
			if (isEqualToGroupImage(group, inputImage)) {
				return;
			}
			imageService.deleteImage(group.getGroupImage());
		}

		saveGroupImage(inputImage, group);
		log.info("success to edit group image");
	}

	private void saveGroupImage(MultipartFile inputImage, StudyGroup group) {
		String imageUrl = imageService.saveImage(ImageType.GROUP, String.valueOf(group.getId()), inputImage);
		group.editGroupImage(imageUrl);
	}

	private void handleNullInputImage(StudyGroup group) {
		if (group.getGroupImage() != null) {
			imageService.deleteImage(group.getGroupImage());
			group.editGroupImage(null);
		}
	}

	private boolean isEqualToGroupImage(StudyGroup group, MultipartFile inputImage) {
		String inputImageUrl = imageService.getImageName(ImageType.GROUP, String.valueOf(group.getId()), inputImage);
		String groupImageUrl = URLDecoder.decode(imageService.parseImageName(group.getGroupImage()),
			StandardCharsets.UTF_8);
		return inputImageUrl.equals(groupImageUrl);
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
				groupMember.getUser(), group, BOJResultConstants.CORRECT);
			Long problems = problemRepository.countProblemsByGroup(group);
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
	public GroupCodeResponse getGroupCode(User user, Long groupId) {
		StudyGroup studyGroup = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾지 못했습니다."));
		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, studyGroup)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		if (RoleOfGroupMember.isOwner(member) || RoleOfGroupMember.isAdmin(member))
			return new GroupCodeResponse(studyGroup.getGroupCode());
		else
			throw new UserValidationException("초대 코드를 조회할 권한이 없습니다.");
	}

	@Transactional(readOnly = true)
	public GetStudyGroupWithCodeResponse getGroupByCode(String code) {
		StudyGroup group = groupRepository.findByGroupCode(code)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));
		User owner = getStudyGroupOwner(group);
		return GetStudyGroupWithCodeResponse.toDTO(group, owner);
	}

	@Transactional(readOnly = true)
	public GetGroupResponse getGroup(User user, Long groupId) {

		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("그룹을 찾을 수 없습니다."));

		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(
				() -> new GroupMemberValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다."));

		GetGroupResponse response = new GetGroupResponse(group.getId(), group.getName(), group.getStartDate(),
			group.getEndDate(), group.getIntroduction(), group.getGroupImage(), member.getRole(),
			getStudyGroupOwner(group).getNickname());
		log.info("success to get study group");
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
	public UpdateBookmarkResponse updateBookmarkGroup(User user, Long groupId) {
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹 입니다."));

		if (!groupMemberRepository.existsByUserAndStudyGroup(user, studyGroup))
			throw new StudyGroupValidationException(HttpStatus.BAD_REQUEST.value(), "참여하지 않은 그룹 입니다.");

		Optional<BookmarkedStudyGroup> bookmarked = bookmarkedStudyGroupRepository.findByUserAndStudyGroup(user,
			studyGroup);

		if (bookmarked.isEmpty()) {
			bookmarkedStudyGroupRepository.save(
				BookmarkedStudyGroup.builder().studyGroup(studyGroup).user(user).build());
			return new UpdateBookmarkResponse(BookmarkStatus.BOOKMARKED);
		} else {
			bookmarkedStudyGroupRepository.delete(bookmarked.get());
			return new UpdateBookmarkResponse(BookmarkStatus.UNMARKED);
		}
	}

	private boolean isBookmarked(User user, StudyGroup group) {
		return bookmarkedStudyGroupRepository.existsByUserAndStudyGroup(user, group);
	}

	@Transactional
	public void updateGroupMemberRole(User user, Long groupId, UpdateGroupMemberRoleRequest request) {
		StudyGroup group = studyGroupRepository.findById(groupId)
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

		if (RoleOfGroupMember.isOwner(member)) {
			owner.updateRole(RoleOfGroupMember.PARTICIPANT);
		}
		log.info("success to update group member role");
	}

	@Transactional(readOnly = true)
	public GroupRoleResponse getRoleInGroup(User user, Long groupId) {
		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));

		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(() -> new GroupMemberValidationException(HttpStatus.NOT_FOUND.value(), "참여하지 않은 그룹입니다."));

		return new GroupRoleResponse(member.getRole().getValue());
	}

	@Transactional
	public void editStudyGroupVisibility(User user, Long groupId, EditGroupVisibilityRequest request) {
		StudyGroup group = groupRepository.findById(groupId)
			.orElseThrow(() -> new CannotFoundGroupException("존재하지 않는 그룹입니다."));

		GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
			.orElseThrow(() -> new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 그룹입니다."));

		member.updateVisibility(request.isVisible());
		log.info("success to update group visibility ( userId : {} )", user.getId());
	}

	@Transactional(readOnly = true)
	public GetStudyGroupListsResponse getOtherStudyGroupList(String userNickname) {
		User targetUser = userRepository.findByNickname(userNickname)
			.orElseThrow(() -> new CannotFoundUserException(HttpStatus.NOT_FOUND.value(), "존재하지 않는 유저입니다."));
		List<StudyGroup> groups = groupRepository.findAllByUser(targetUser);

		LocalDate today = LocalDate.now();

		List<GetStudyGroupResponse> bookmarked = groups.stream()
			.filter(group -> isBookmarked(targetUser, group) && isVisible(group, targetUser))
			.map(group -> getStudyGroupResponseDTO(targetUser, group))
			.toList();

		List<GetStudyGroupResponse> done = groups.stream()
			.filter(group -> isDone(today, group) && isVisible(group, targetUser))
			.map(group -> getStudyGroupResponseDTO(targetUser, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		List<GetStudyGroupResponse> inProgress = groups.stream()
			.filter(
				group -> isInProgress(today, group) && isVisible(group, targetUser))
			.map(group -> getStudyGroupResponseDTO(targetUser, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		List<GetStudyGroupResponse> queued = groups.stream()
			.filter(group -> isQueued(today, group) && isVisible(group, targetUser))
			.map(group -> getStudyGroupResponseDTO(targetUser, group))
			.filter(response -> !bookmarked.contains(response))
			.toList();

		GetStudyGroupListsResponse response = new GetStudyGroupListsResponse(bookmarked, done, inProgress, queued);

		log.info("success to get study group list");
		return response;
	}

	private boolean isVisible(StudyGroup group, User user) {
		return groupMemberRepository.existsByUserAndStudyGroupAndIsVisible(user, group, true);
	}

	private void sendNewMemberNotification(StudyGroup studyGroup, GroupMember newMember) {
		List<GroupMember> members = groupMemberRepository.findAllByStudyGroup(studyGroup)
			.stream()
			.filter(member -> !member.getId().equals(newMember.getId()))
			.toList();

		notificationService.sendNotificationToMembers(
			studyGroup,
			members,
			null, null,
			NotificationCategory.NEW_MEMBER_JOINED,
			NotificationCategory.NEW_MEMBER_JOINED.getMessage(newMember.getUser().getNickname())
		);
	}

	@Transactional(readOnly = true)
	public List<GetGroupSettingResponse> getStudyGroupSettings(User user) {
		List<StudyGroup> groups = groupRepository.findAllByUser(user);

		List<GetGroupSettingResponse> response = groups.stream().map(group -> {
			GroupMember member = groupMemberRepository.findByUserAndStudyGroup(user, group)
				.orElseThrow(
					() -> new GroupMemberValidationException(HttpStatus.FORBIDDEN.value(), "참여하지 않은 스터디 그룹입니다."));
			return GetGroupSettingResponse.toDto(group, member, isBookmarked(user, group), member.getIsVisible());
		}).toList();
		log.info("success to get my study groups settings");
		return response;
	}
}
