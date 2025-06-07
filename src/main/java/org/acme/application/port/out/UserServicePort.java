package org.acme.application.port.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public interface UserServicePort {
    boolean userExists(String userId);
    UserDto getUserById(String userId);
    boolean areUsersFriends(String userId1, String userId2);
    List<String> getUserFriends(String userId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserDto {
        public String id;
        public String username;
        public String email;
        public String displayName;
        public Boolean isActive;
        public LocalDateTime lastLogin;
    }
}