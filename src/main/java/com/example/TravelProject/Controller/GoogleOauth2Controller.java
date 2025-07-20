package com.example.TravelProject.Controller;

import com.example.TravelProject.Jwt.JwtProvider;
import com.example.TravelProject.Repository.UserAccount.SocialAccountRepository;
import com.example.TravelProject.Repository.UserAccount.UserRepository;
import com.example.TravelProject.entity.UserAccount.SocialAccount;
import com.example.TravelProject.entity.UserAccount.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/app/oauth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
public class GoogleOauth2Controller {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/google")
    public ResponseEntity<?> googleCallback(@RequestBody Map<String, String> tokenData) {
        try {
            String accessToken = tokenData.get("access_token");
            System.out.println("accessToken:"+accessToken);
            // Google API를 사용해 사용자 정보 가져오기
            String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
            Map<String, Object> userInfo = restTemplate.getForObject(userInfoUrl, Map.class);
            System.out.println("userInfoUrl: "+userInfoUrl);

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");
            String providerId = (String) userInfo.get("id");
            String provider = "google";

            // 유저 존재 여부 확인
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .username(name)
                        .userRole("USER")
                        .build();
                return userRepository.save(newUser);
            });

                // JWT 발급
            String jwtAccessToken = jwtProvider.createAccessToken(email);
            String refreshToken = jwtProvider.createRefreshToken(email);
            System.out.println(jwtAccessToken+"+"+refreshToken);
            // 소셜 계정 존재 여부 확인
            Optional<SocialAccount> socialOpt = socialAccountRepository.findByUserAndProvider(user, provider);
            if (socialOpt.isEmpty()) {
                SocialAccount socialAccount = SocialAccount.builder()
                        .user(user)
                        .provider(provider)
                        .last_synced_date(LocalDateTime.now())
                        .build();
                socialAccountRepository.save(socialAccount);
            }
            System.out.println(jwtAccessToken+"+"+refreshToken+"+"+email+"+"+name);
            return ResponseEntity.ok(Map.of(
                    "accessToken", jwtAccessToken,
                    "refreshToken", refreshToken,
                    "email", email,
                    "username", name
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication failed: " + e.getMessage());
        }
    }
}