package zerobase.dividend.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

public class MemberException {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoMemberException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "존재하지 않는 ID 입니다. " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.BAD_REQUEST.value();
        }
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrongPasswordException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "비밀번호가 일치하지 않습니다. " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.BAD_REQUEST.value();
        }
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlreadyExistUserException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "이미 존재하는 사용자명입니다. " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.BAD_REQUEST.value();
        }
    }
}
