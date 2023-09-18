package fc.server.palette.member.auth.Controller;

import fc.server.palette.member.auth.CustomUserDetailService;
import fc.server.palette.member.auth.dto.LoginDto;
import fc.server.palette.member.auth.dto.TokenDto;
import fc.server.palette.member.auth.jwt.AuthoritiesProvider;
import fc.server.palette.member.auth.jwt.JwtFilter;
import fc.server.palette.member.auth.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final TokenProvider tokenProvider;
    private final CustomUserDetailService customUserDetailService;

    @PostMapping("/login")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginDto loginDto) {

        UserDetails userDetails = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        // 패스워드검증부분
        if(!customUserDetailService.isSamePwd(Long.valueOf(userDetails.getUsername()),
        loginDto.getPassword())){
            return new ResponseEntity<>("이메일 혹은 패스워드가 일치하지 않습니다.",HttpStatus.BAD_REQUEST);
        }
        UsernamePasswordAuthenticationToken  authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                AuthoritiesProvider.getAuthorityCollection()
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        String jwt = tokenProvider.createToken(authenticationToken);


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer" + jwt);

        return new ResponseEntity<>(new TokenDto(jwt), httpHeaders, HttpStatus.OK);
    }


}
