package com.gamzabat.algohub.feature.studygroup.controller;

import java.util.List;

import com.gamzabat.algohub.feature.studygroup.dto.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gamzabat.algohub.common.annotation.AuthedUser;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.exception.RequestException;
import com.gamzabat.algohub.feature.studygroup.service.StudyGroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group")
@Tag(name = "그룹 API", description = "스터디 그룹 관련 API")
public class StudyGroupController {
	private final StudyGroupService studyGroupService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "그룹 생성 API")
	public ResponseEntity<CreateGroupResponse> createGroup(@AuthedUser User user,
		@Valid @RequestPart CreateGroupRequest request, Errors errors, @RequestPart(required = false) MultipartFile profileImage){
		if (errors.hasErrors())
			throw new RequestException("그룹 생성 요청이 올바르지 않습니다.",errors);
		CreateGroupResponse inviteCode = studyGroupService.createGroup(user, request, profileImage);
		return ResponseEntity.ok().body(inviteCode);
	}

	@PostMapping(value = "/{code}/join")
	@Operation(summary = "그룹 코드를 사용한 그룹 참여 API")
	public ResponseEntity<Object> joinGroupWithCode(@AuthedUser User user, @PathVariable String code){
		studyGroupService.joinGroupWithCode(user,code);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping(value = "list")
	@Operation(summary = "그룹 목록 조회 API", description = "방장 여부 상관 없이 유저가 참여하고 있는 그룹 모두 조회")
	public ResponseEntity<GetStudyGroupListsResponse> getStudyGroupList(@AuthedUser User user){
		GetStudyGroupListsResponse response = studyGroupService.getStudyGroupList(user);
		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping(value = "leave")
	@Operation(summary = "그룹 탈퇴 API", description = "방장,멤버 상관 없이 해당 그룹을 삭제,탈퇴하는 API")
	public ResponseEntity<Object> deleteGroup(@AuthedUser User user, @RequestParam Long groupId){
		studyGroupService.deleteGroup(user,groupId);
		return ResponseEntity.ok().body("OK");
	}


	@DeleteMapping(value  = "delete")
	@Operation(summary = "그룹 멤버 삭제", description = "방장만 가능한 그룹 멤버를 삭제하는 API")
	public ResponseEntity<Object> deleteUser(@AuthedUser User user, @RequestParam Long userId, @RequestParam Long groupId) {
		studyGroupService.deleteMember(user,userId,groupId);
		return ResponseEntity.ok().body("OK");
	}

	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "그룹 정보 수정 API")
	public ResponseEntity<Object> editGroup(@AuthedUser User user,
		@Valid @RequestPart EditGroupRequest request, Errors errors,
		@RequestPart(required = false) MultipartFile groupImage){
		if(errors.hasErrors())
			throw new RequestException("그룹 정보 수정 요청이 올바르지 않습니다.",errors);
		studyGroupService.editGroup(user,request,groupImage);
		return ResponseEntity.ok().body("OK");
	}

	@GetMapping(value = "member-list")
	@Operation(summary = "그룹 회원 목록 조회")
	public ResponseEntity<Object> getGroupInfo(@AuthedUser User user, @RequestParam Long groupId) {


		List<GetGroupMemberResponse> members = studyGroupService.groupInfo(user,groupId);
		return ResponseEntity.ok().body(members);
	}

	@GetMapping(value = "problem-solving")
	@Operation(summary = "문제 별 회원 풀이 여부 조회")
	public ResponseEntity<Object> getCheckingSolvedProblem(@AuthedUser User user, @RequestParam Long problemId) {
			List<CheckSolvedProblemResponse> responseList = studyGroupService.getChekingSolvedProblem(user,problemId);
			return ResponseEntity.ok().body(responseList);
	}

	@GetMapping(value = "group-code")
	@Operation(summary = "그룹 초대 코드 조회")
	public ResponseEntity<Object> getGroupCode(@AuthedUser User user, @RequestParam Long groupId) {
		return ResponseEntity.ok().body(studyGroupService.getGroupCode(user,groupId));
	}

	@GetMapping(value = "/{code}")
	@Operation(summary = "그룹 코드를 사용한 그룹 정보 조회 API")
	public ResponseEntity<GetStudyGroupWithCodeResponse> getGroupByCode(@PathVariable String code){
		return ResponseEntity.ok().body(studyGroupService.getGroupByCode(code));
	}

	@GetMapping(value = "top-ranking")
	@Operation(summary = "과제 진행도 상위순위")
	public ResponseEntity<Object> getTopRanking(@AuthedUser User user, @RequestParam Long groupId) {
		List<GetRankingResponse> rankingResponse = studyGroupService.getTopRank(user, groupId);
		return ResponseEntity.ok().body(rankingResponse);
	}

	@GetMapping(value = "all-ranking")
	@Operation(summary = "과제 진행도 전체순위")
	public ResponseEntity<Object> getAllRanking(@AuthedUser User user, @RequestParam Long groupId) {
		List<GetRankingResponse> rankingResponse = studyGroupService.getAllRank(user, groupId);
		return ResponseEntity.ok().body(rankingResponse);
	}

	@GetMapping(value = "group-info")
	@Operation(summary = "그룹정보")
	public ResponseEntity<GetGroupResponse> groupInfo(@AuthedUser User user, @RequestParam Long groupId) {
		GetGroupResponse response = studyGroupService.getGroup(user,groupId);
		return ResponseEntity.ok().body(response);
	}
 }
