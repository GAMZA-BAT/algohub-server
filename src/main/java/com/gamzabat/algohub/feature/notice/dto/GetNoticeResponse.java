package com.gamzabat.algohub.feature.notice.dto;

import java.time.LocalDateTime;

import com.gamzabat.algohub.feature.notice.domain.Notice;

import lombok.Builder;

@Builder
public record GetNoticeResponse(String author,
								String authorImage,
								Long noticeId,
								String content,
								String title,
								String category,
								LocalDateTime createdAt,
								boolean isRead) {

	public static GetNoticeResponse toDTO(Notice notice, boolean isRead) {
		return GetNoticeResponse.builder()
			.author(notice.getAuthor().getNickname())
			.authorImage(notice.getAuthor().getProfileImage())
			.noticeId(notice.getId())
			.title(notice.getTitle())
			.content(notice.getContent())
			.category(notice.getCategory())
			.createdAt(notice.getCreatedAt())
			.isRead(isRead)
			.build();
	}
}
