package com.gamzabat.algohub.feature.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.gamzabat.algohub.feature.group.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.problem.domain.Problem;
import com.gamzabat.algohub.feature.user.domain.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByUser(User user);

	List<Notification> findAllByUserAndIsRead(User user, boolean isRead);

	@Modifying
	@Query("delete from Notification n where n.studyGroup = :studyGroup")
	void deleteAllByStudyGroup(StudyGroup studyGroup);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.problem = :problem")
    void deleteAllByProblem(Problem problem);

	@Modifying
	@Query("DELETE FROM Notification n WHERE n.user = :user AND n.studyGroup = :studyGroup")
	void deleteAllByUserAndStudyGroup(User user, StudyGroup studyGroup);
}
