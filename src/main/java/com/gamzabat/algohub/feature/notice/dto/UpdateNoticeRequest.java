package com.gamzabat.algohub.feature.notice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoticeRequest(@NotBlank(message = "제목은 필수 입력입니다") String title,
								  @NotBlank(message = "본문은 필수 입력입니다") String content,
								  @NotBlank(message = "카테고리는 필수 입력입니다") String category
) {
}
