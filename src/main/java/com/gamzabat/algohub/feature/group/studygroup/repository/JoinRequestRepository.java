package com.gamzabat.algohub.feature.group.studygroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

	boolean existsByGroup_IdAndRequester_Id(Long groupId, Long userId);

	@Query("""
		select jr
		from JoinRequest jr
		join fetch jr.requester u
		join fetch jr.group g
		where g.id = :groupId
		""")
	List<JoinRequest> findAllByGroupIdWithFetch(@Param("groupId") Long groupId);

}
