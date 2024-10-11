package com.gamzabat.algohub.feature.group.studygroup.etc;

import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.feature.group.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.group.studygroup.exception.InvalidRoleException;

import lombok.Getter;

@Getter
public enum RoleOfGroupMember {
	OWNER("OWNER"),
	ADMIN("ADMIN"),
	PARTICIPANT("PARTICIPANT");

	private final String value;

	RoleOfGroupMember(String value) {
		this.value = value;
	}

	public static RoleOfGroupMember fromValue(String value) {
		for (RoleOfGroupMember role : RoleOfGroupMember.values()) {
			if (role.value.equals(value)) {
				return role;
			}
		}
		throw new InvalidRoleException(HttpStatus.BAD_REQUEST.value(), "해당 ROLE은 존재하지 않습니다.");
	}

	public static boolean isOwner(GroupMember groupMember) {
		return groupMember.getRole().equals(OWNER);
	}

	public static boolean isAdmin(GroupMember groupMember) {
		return groupMember.getRole().equals(ADMIN);
	}

	public static boolean isParticipant(GroupMember groupMember) {
		return groupMember.getRole().equals(PARTICIPANT);
	}
}
