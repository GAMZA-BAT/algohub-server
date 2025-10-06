package com.gamzabat.algohub.feature.edgecase.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
	public record CreateEdgeCaseRequest(@NotBlank(message = "문제 링크 입력은 필수입니다.")  String link,
										@NotBlank(message = "반례 input 입력은 필수입니다.")  String input,
										@NotBlank(message = "반례 output 입력은 필수입니다.")  String output){
}
