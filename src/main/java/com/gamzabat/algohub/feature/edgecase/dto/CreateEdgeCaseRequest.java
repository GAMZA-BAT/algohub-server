package com.gamzabat.algohub.feature.edgecase.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEdgeCaseRequest(@NotBlank(message = "문제 티어 입력은 필수입니다.") Integer level,
									@NotBlank(message = "문제 링크 입력은 필수입니다.")  String link,
									@NotBlank(message = "문제 번호 입력은 필수입니다.")  Integer number,
									@NotBlank(message = "문제 이름 입력은 필수입니다.") String tile,
									@NotBlank(message = "빈례 input 입력은 필수입니다.")  String input,
									@NotBlank(message = "반례 output 입력은 필수입니다.")  String output){
}
