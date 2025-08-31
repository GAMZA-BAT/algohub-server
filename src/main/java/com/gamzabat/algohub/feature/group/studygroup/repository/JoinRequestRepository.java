package com.gamzabat.algohub.feature.group.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.group.studygroup.domain.JoinRequest;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

}
