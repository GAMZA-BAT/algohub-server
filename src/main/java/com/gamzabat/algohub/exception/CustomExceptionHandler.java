package com.gamzabat.algohub.exception;

import com.gamzabat.algohub.feature.comment.exception.CommentValidationException;
import com.gamzabat.algohub.feature.comment.exception.SolutionValidationException;
import com.gamzabat.algohub.feature.problem.exception.NotBojLinkException;
import com.gamzabat.algohub.feature.solution.exception.CannotFoundSolutionException;
import com.gamzabat.algohub.feature.studygroup.exception.CannotFoundGroupException;
import com.gamzabat.algohub.feature.studygroup.exception.GroupMemberValidationException;
import com.gamzabat.algohub.feature.user.exception.UncorrectedPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {
	@ExceptionHandler(RequestException.class)
	protected ResponseEntity<Object> handler(RequestException e){
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getError(), e.getMessages()));
	}

	@ExceptionHandler(UserValidationException.class)
	protected ResponseEntity<Object> handler(UserValidationException e){
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getErrors(), null));
	}

	@ExceptionHandler(StudyGroupValidationException.class)
	protected ResponseEntity<Object> handler(StudyGroupValidationException e){
		return ResponseEntity.status(e.getCode()).body(new ErrorResponse(e.getCode(), e.getError(), null));
	}
	@ExceptionHandler(GroupMemberValidationException.class)
	protected ResponseEntity<Object> handler(GroupMemberValidationException e){
		return ResponseEntity.status(e.getCode()).body(new ErrorResponse(e.getCode(), e.getError(), null));
	}

	@ExceptionHandler(ProblemValidationException.class)
	protected  ResponseEntity<Object> handler(ProblemValidationException e) {
		return ResponseEntity.status(e.getCode()).body(new ErrorResponse(e.getCode(), e.getError(), null));
	}

	@ExceptionHandler(UncorrectedPasswordException.class)
	protected ResponseEntity<Object> handler(UncorrectedPasswordException e){
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getErrors(), null));
	}

	@ExceptionHandler(SolutionValidationException.class)
	protected ResponseEntity<Object> handler(SolutionValidationException e){
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getError(), null));
	}

	@ExceptionHandler(CommentValidationException.class)
	protected ResponseEntity<Object> handler(CommentValidationException e){
		return ResponseEntity.status(e.getCode()).body(new ErrorResponse(e.getCode(), e.getError(), null));
	}

	@ExceptionHandler(CannotFoundGroupException.class)
	protected  ResponseEntity<Object> handler(CannotFoundGroupException e) {
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getErrors(), null));
	}
	@ExceptionHandler(NotBojLinkException.class)
	protected ResponseEntity<Object> handler(NotBojLinkException e){
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),e.getError(),null));
	}
	@ExceptionHandler(CannotFoundSolutionException.class)
	protected ResponseEntity<Object> handler(CannotFoundSolutionException e) {
		return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),e.getErrors(),null));
	}
}
