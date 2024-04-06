package zerobase.dividend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.dividend.entity.MemberEntity;
import zerobase.dividend.model.Auth;
import zerobase.dividend.security.TokenProvider;
import zerobase.dividend.service.MemberService;

@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
@Slf4j
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;
    
    // 회원가입을 위한 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        MemberEntity registered = memberService.register(request);
        return ResponseEntity.ok(registered);
    }
    
    // 로그인용 API
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        MemberEntity memberEntity = memberService.authenticate(request);
        String token = tokenProvider.generateToken(
                memberEntity.getUsername(), memberEntity.getRoles());
        
        return ResponseEntity.ok(token);
    }
}