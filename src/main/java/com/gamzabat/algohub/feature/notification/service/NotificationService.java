package com.gamzabat.algohub.feature.notification.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.gamzabat.algohub.exception.UserValidationException;
import com.gamzabat.algohub.feature.notification.domain.Notification;
import com.gamzabat.algohub.feature.notification.dto.GetNotificationResponse;
import com.gamzabat.algohub.feature.notification.repository.EmitterRepositoryImpl;
import com.gamzabat.algohub.feature.notification.repository.NotificationRepository;
import com.gamzabat.algohub.feature.studygroup.domain.StudyGroup;
import com.gamzabat.algohub.feature.user.domain.User;
import com.gamzabat.algohub.feature.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
	private final EmitterRepositoryImpl emitterRepository;
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	@Transactional
	public SseEmitter subscribe(User user, String lastEventId) {
		String email = user.getEmail();
		String emitterId = makeTimeIncludedId(email);
		SseEmitter emitter;

		if(emitterRepository.findAllEmitterStartWithByEmail(email) != null)
			emitterRepository.deleteAllEmitterStartWithId(email);

		emitter = emitterRepository.save(emitterId, new SseEmitter(Long.MAX_VALUE));

		emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
		emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
		emitter.onError((e) -> emitterRepository.deleteById(emitterId));
		emitter.onError((e) -> emitterRepository.deleteById(emitterId));

		String eventId = makeTimeIncludedId(email);
		sendNotification(emitter, eventId, emitterId, "EventStream Created : [userId = "+email+"]");

		if(hasLostData(lastEventId))
			sendLostData(lastEventId, email, emitterId, emitter);

		return emitter;
	}

	private String makeTimeIncludedId(String email) {
		return email+"_"+System.currentTimeMillis();
	}

	@Transactional
	public void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data){
		try{
			emitter.send(SseEmitter.event()
				.id(eventId)
				.name("sse")
				.data(data, MediaType.APPLICATION_JSON));
		}catch (IOException e){
			emitterRepository.deleteById(emitterId);
			emitter.completeWithError(e);
		}
	}

	private boolean hasLostData(String lastEventId){
		return !lastEventId.isEmpty();
	}

	private void sendLostData(String lastEventId, String email, String emitterId, SseEmitter emitter){
		Map<String,Object> eventCaches = emitterRepository.findAllEventCacheStartWithByEmail(email);
		eventCaches.entrySet().stream()
			.filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
			.forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
	}

	@Transactional
	public void send(String receiver, String message, StudyGroup studyGroup, String subContent){
			Notification notification = createNotification(receiver, message, studyGroup, subContent);
			notificationRepository.save(notification);
			Map<String,SseEmitter> sseEmitter = emitterRepository.findAllEmitterStartWithByEmail(receiver);
			sseEmitter.forEach(
				(key,emitter) -> {
					emitterRepository.saveEventCache(key, notification);
					sendToClient(emitter, key, notification);
				}
			);
	}

	@Transactional
	public void sendList(List receiverList, String message, StudyGroup studyGroup, String subContent){
		List<Notification> notifications = new ArrayList<>();
		Map<String,SseEmitter> sseEmitters;
		for(int i=0; i<receiverList.size(); i++){
			int finalI = i;
			sseEmitters = new HashMap<>();
			Notification notification = createNotification(receiverList.get(i).toString(), message, studyGroup, subContent);
			notifications.add(notification);
			notificationRepository.save(notification);
			sseEmitters.putAll(emitterRepository.findAllEmitterStartWithByEmail(receiverList.get(i).toString()));
			sseEmitters.forEach(
				(key,emitter)->{
					emitterRepository.saveEventCache(key, notifications.get(finalI));
					sendToClient(emitter, key, notifications.get(finalI));
				}
			);
		}
	}

	private Notification createNotification(String receiver, String message, StudyGroup studyGroup, String subContent){
		return Notification.builder()
			.user(userRepository.findByEmail(receiver).orElseThrow(() -> new UserValidationException("존재 하지 않는 회원 입니다.")))
			.message(message)
			.studyGroup(studyGroup)
			.subContent(subContent)
			.isRead(false)
			.build();
	}

	private void sendToClient(SseEmitter emitter, String id, Notification notification){
		try{
			emitter.send(SseEmitter.event()
				.id(id)
				.name("sse")
				.data(GetNotificationResponse.toDTO(notification),MediaType.APPLICATION_JSON));
		}catch (Exception e){
			emitterRepository.deleteById(id);
			emitter.completeWithError(e);
		}
	}

	@Transactional(readOnly = true)
	public List<GetNotificationResponse> getNotifications(User user) {
		List<Notification> notifications = notificationRepository.findAllByUser(user);
		notifications.sort(Comparator.comparingLong(Notification::getId).reversed());
		return notifications.stream().map(GetNotificationResponse::toDTO).toList();
	}

	public void updateIsRead(User user) {
		List<Notification> notifications = notificationRepository.findAllByUserAndIsRead(user,false);
		notifications.forEach(notification -> {
			notification.updateIsRead();
			notificationRepository.save(notification);
		});
		log.info("success to read status");
	}
}
