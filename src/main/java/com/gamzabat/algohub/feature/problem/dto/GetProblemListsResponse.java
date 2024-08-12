package com.gamzabat.algohub.feature.problem.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class GetProblemListsResponse {
    private List<GetProblemResponse> inProgressProblems;
    private List<GetProblemResponse> expiredProblems;
    private int currentPage;
    private int totalPages;
    private long totalItems;

    public GetProblemListsResponse(List<GetProblemResponse> inProgressProblems, List<GetProblemResponse> completedProblems, int currentPage, int totalPages, long totalItems) {
        this.inProgressProblems = inProgressProblems;
        this.expiredProblems = completedProblems;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }

}
