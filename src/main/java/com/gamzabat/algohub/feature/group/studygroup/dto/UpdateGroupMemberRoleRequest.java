package com.gamzabat.algohub.feature.group.studygroup.dto;

import com.gamzabat.algohub.common.annotation.ValidEnum;
import com.gamzabat.algohub.feature.group.studygroup.etc.RoleOfGroupMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateGroupMemberRoleRequest(@NotNull(message = "스터디 그룹 고유 아이디는 필수 입력 입니다.") Long studyGroupId,
										   @NotNull(message = "스터디 그룹 멤버의 고유 아이디는 필수 입력 입니다.") Long memberId,
										   @NotBlank(message = "변경할 역할은 필수 입력 입니다.")
										   @ValidEnum(enumClass = RoleOfGroupMember.class, message = "올바른 enum 값을 입력해주세요. (ADMIN, PARTICIPANT)")
										   String role) {
}
