package zerobase.dividend.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

public class CompanyException {
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlreadyExistTickerException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "already exists ticker " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.CONFLICT.value();
        }
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoCompanyException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "존재하지 않는 회사명입니다. " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.BAD_REQUEST.value();
        }
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedScrapTickerException extends AbstractException {
        private String message = "";
        
        @Override
        public String getMessage() {
            return "failed to scrap ticker " + message;
        }
        
        @Override
        public int getStatusCode() {
            return HttpStatus.BAD_REQUEST.value();
        }
    }
}
