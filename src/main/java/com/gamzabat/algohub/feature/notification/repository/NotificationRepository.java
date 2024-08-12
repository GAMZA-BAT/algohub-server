package com.gamzabat.algohub.feature.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.user.domain.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByUser(User user);


	List<Notification> findAllByUserAndIsRead(User user, boolean isRead);
}
