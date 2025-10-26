package com.gamzabat.algohub.feature.group.studygroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

	boolean existsByGroup_IdAndRequester_Id(Long groupId, Long userId);

	List<JoinRequest> findAllByGroup_Id(Long groupId);
}
