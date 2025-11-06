package controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import entity.SignUpEntity;
import repository.SignUpRepository;




@RestController
@RequestMapping("/api/microsoft")
public class MicrosoftSessionController {

    @Autowired
    private SignUpRepository userRepository;
    
    @GetMapping("/sso-users")
    public ResponseEntity<?> getMicrosoftSSOUsers() {
        List<SignUpEntity> microsoftUsers = userRepository.findByProvider("microsoft");
        
        List<Map<String, Object>> users = microsoftUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("name", user.getName());
//                    userMap.put("provider", user.getProvider());
//                    userMap.put("providerId", user.getProviderId());
//                    userMap.put("jobTitle", user.getJobTitle());
//                    userMap.put("department", user.getDepartment());
                    return userMap;
                })
                .toList();
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/sso-sessions")
    public ResponseEntity<?> getActiveMicrosoftSessions() {
        // This would track active Microsoft SSO sessions
        // For now, return Microsoft users count
        long microsoftUsersCount = userRepository.findByProvider("microsoft").size();
        
        Map<String, Object> response = new HashMap<>();
        response.put("activeMicrosoftSessions", microsoftUsersCount);
        response.put("message", "Microsoft SSO session tracking");
        
        return ResponseEntity.ok(response);
    }
}