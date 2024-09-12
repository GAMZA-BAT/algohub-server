package com.gamzabat.algohub.feature.studygroup.etc;

import org.springframework.http.HttpStatus;

import com.gamzabat.algohub.feature.studygroup.domain.GroupMember;
import com.gamzabat.algohub.feature.studygroup.exception.InvalidRoleException;

public enum RoleOfGroupMember {
	OWNER("OWNER"),
	ADMIN("ADMIN"),
	PARTICIPANT("PARTICIPANT");

	private final String value;

	RoleOfGroupMember(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
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
