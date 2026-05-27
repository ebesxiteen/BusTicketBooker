package com.example.ticketbooker.Service.ServiceImp;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.ticketbooker.Entity.CustomOAuth2User;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.UserService;

@Service
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    @Lazy // Dùng Lazy để tránh vòng lặp bean nếu UserService cũng gọi lại security
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. Lấy thông tin thô từ Google/Facebook/Github
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String name = (String) attributes.get("name");
        String email = (String) attributes.get("email");

        // 2. Xử lý logic cho GitHub (vì Github thường ẩn email)
        if (email == null && userRequest.getClientRegistration().getRegistrationId().equals("github")) {
            email = getGithubEmail(userRequest.getAccessToken().getTokenValue());
            if (name == null) name = (String) attributes.get("login"); // Lấy username làm tên
        }

        System.out.println("OAuth2 Login: " + email + " - " + name);

        // 3. GỌI HÀM XỬ LÝ LƯU USER VÀO DATABASE (QUAN TRỌNG)
        if (email != null) {
            userService.processOAuthPostLogin(email, name);
        }

        // 4. Lấy lại User mới nhất từ DB để tạo CustomOAuth2User
        Users user = userRepo.findByEmail(email);
        
        // Trả về CustomOAuth2User (đã sửa ở bước trước để chứa Users entity)
        return new CustomOAuth2User(oAuth2User, user);
    }

    // Hàm phụ: Lấy email từ GitHub API 
    private String getGithubEmail(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            var headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(accessToken);
            var entity = new org.springframework.http.HttpEntity<>(headers);
            var response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {} 
            );
            
            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                return emails.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                        .map(e -> e.get("email").toString())
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            System.out.println("Error getting Github email: " + e.getMessage());
        }
        return null;
    }
}