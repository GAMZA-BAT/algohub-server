package com.gamzabat.algohub.feature.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNoticeRequest(@NotNull(message = "그룹 id 는 필수입니다") Long studyGroupId,
								  @NotBlank(message = "제목은 필수 입력입니다") String title,
								  @NotBlank(message = "본문은 필수 입력입니다") String content,
								  @NotBlank(message = "카테고리는 필수 입력입니다") String category
) {

}
