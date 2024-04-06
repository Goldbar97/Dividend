package zerobase.dividend.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import zerobase.dividend.entity.MemberEntity;
import zerobase.dividend.exception.MemberException;
import zerobase.dividend.model.Auth;
import zerobase.dividend.repository.MemberRepository;

@AllArgsConstructor
@Service
@Slf4j
public class MemberService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    
    public MemberEntity authenticate(Auth.SignIn requestMember) {
        MemberEntity memberEntity = memberRepository.findByUsername(
                        requestMember.getUsername())
                .orElseThrow(MemberException.NoMemberException::new);
        
        if (!passwordEncoder.matches(
                requestMember.getPassword(), memberEntity.getPassword())) {
            throw new MemberException.WrongPasswordException();
        }
        
        return memberEntity;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return memberRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(
                        "couldn't find user -> " + username));
    }
    
    public MemberEntity register(Auth.SignUp requestMember) {
        boolean exists = memberRepository.existsByUsername(
                requestMember.getUsername());
        if (exists) {
            throw new MemberException.AlreadyExistUserException();
        }
        
        requestMember.setPassword(
                passwordEncoder.encode(requestMember.getPassword()));
        MemberEntity saved = memberRepository.save(requestMember.toEntity());
        
        return saved;
    }
}
