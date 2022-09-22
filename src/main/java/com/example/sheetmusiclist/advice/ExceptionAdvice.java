package com.example.sheetmusiclist.advice;


import com.example.sheetmusiclist.exception.*;
import com.example.sheetmusiclist.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.relation.RoleNotFoundException;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {
    // 500 에러
    // 컨버트 실패
    @ExceptionHandler(IllegalArgumentException.class) // 지정한 예외가 발생하면 해당 메소드 실행
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 각 예외마다 상태 코드 지정
    public Response illegalArgumentExceptionAdvice(IllegalArgumentException e) {
        log.info("e = {}", e.getMessage());
        return Response.failure(500, e.getMessage().toString());
    }

    // 500 에러
    // 컨버트 실패
    @ExceptionHandler(CannotConvertHelperException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response cannotConvertNestedStructureException(CannotConvertHelperException e) {
        log.error("e = {}", e.getMessage());
        return Response.failure(500, e.getMessage().toString());
    }

    // 400 에러
    // validation, MethodArgumentNotValidException
    // 각 검증 어노테이션 별로 지정해놨던 메시지를 응답
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response methodArgumentNotValidException(MethodArgumentNotValidException e) { // 2
        return Response.failure(400, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    // 400
    // 토큰 만료
    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response tokenExpiredException() {
        return Response.failure(400, "토큰이 만료되었습니다.");
    }

    // 400 에러
    // Valid 제약조건 위배 캐치
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response bindException(BindException e) {
        return Response.failure(400, e.getBindingResult().getFieldError().getDefaultMessage());
    }





    // 401 응답
    // 아이디 혹은 비밀번호 오류시
    @ExceptionHandler(LoginFailureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response loginFailureException() {
        return Response.failure(401, "로그인에 실패하였습니다.");
    }

    // 401 응답
    // 유저 정보가 일치하지 않음
    @ExceptionHandler(MemberNotEqualsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response memberNotEqualsException() {
        return Response.failure(401, "유저 정보가 일치하지 않습니다.");
    }

    // 404 응답
    // 요청한 User를 찾을 수 없음
    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response memberNotFoundException() {
        return Response.failure(404, "요청한 회원을 찾을 수 없습니다.");
    }


    // 404 응답
    // 요청한 자원을 찾을 수 없음
    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response roleNotFoundException() {
        return Response.failure(404, "요청한 권한 등급을 찾을 수 없습니다.");
    }

    // 404 응답
    // 매칭을 찾을 수 없음
    @ExceptionHandler(MatchingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response matchingNotFoundException() {
        return Response.failure(404, "요청한 매칭을 찾을 수 없습니다.");
    }

    // 409 응답
    // username 중복
    @ExceptionHandler(MemberUsernameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response memberEmailAlreadyExistsException(MemberUsernameAlreadyExistsException e) {
        return Response.failure(409, e.getMessage() + "은 중복된 아이디 입니다.");
    }

    // 409 응답
    // nickname 중복
    @ExceptionHandler(MemberNicknameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response memberNicknameAlreadyExistsException(MemberNicknameAlreadyExistsException e) {
        return Response.failure(409, e.getMessage() + "은 중복된 닉네임 입니다.");
    }

    // 409 응답
    // email 중복
    @ExceptionHandler(MemberEmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response memberEmailAlreadyExistsException(MemberEmailAlreadyExistsException e) {
        return Response.failure(409, e.getMessage() + "은 중복된 이메일 입니다.");
    }

    // 409 응답
    // phone 중복
    @ExceptionHandler(MemberPhoneAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response memberPhoneAlreadyExistsException(MemberPhoneAlreadyExistsException e) {
        return Response.failure(409, e.getMessage() + "은 중복된 번호 입니다.");
    }


    // 409 응답
    // 이미 유저와 상품이 매치된 경우
    @ExceptionHandler(AlreadyMatchedUserAndProduct.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response alreadyMatchedUserAndProduct() {
        return Response.failure(400, "이미 매칭이 신청되었습니다.");
    }

    //에러코드 수정해야함 시간날때 에러코드 확인점
    @ExceptionHandler(FileUploadFailureException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response fileUploadFailureException(){
        return Response.failure(404,"파일 업로드에 실패하였습니다.");
    }

    //에러코드 확인점
    @ExceptionHandler(UnsupportedImageFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response unsupportedImageFormatException(){
        return Response.failure(404,"이미지형식이 맞지않습니다.");
    }

}
