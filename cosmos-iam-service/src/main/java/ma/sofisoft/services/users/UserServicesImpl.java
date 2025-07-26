package ma.sofisoft.services.users;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.users.CreateUserRequest;
import ma.sofisoft.dtos.users.UpdateUserRequest;
import ma.sofisoft.dtos.users.UserResponse;
import ma.sofisoft.services.KeycloakService;

import java.util.List;

@Slf4j
@ApplicationScoped
public class UserServicesImpl implements UserServices {

    @Inject
    KeycloakService keycloakService;

    @Override
    public String createUser(String token, CreateUserRequest request) {
        return keycloakService.createUser(token, request);
    }

    @Override
    public void updateUserInfo(String token, String userId, UpdateUserRequest request) {
        keycloakService.updateUserInfo(token, userId, request);
    }

    @Override
    public void updateUserPassword(String token, String username, String newPassword) {
        keycloakService.updateUserPassword(token, username, newPassword);
    }

    @Override
    public void deleteUser(String token, String userId) {
        keycloakService.deleteUser(token, userId);
    }

    @Override
    public List<UserResponse> getUsers(String token) {
        return keycloakService.getUsers(token);
    }

    @Override
    public UserResponse getUserById(String token, String userId) {
        return keycloakService.getUserById(token, userId);
    }

    @Override
    public void enableUser(String token, String userId, boolean enabled) {
        keycloakService.enableUser(token, userId, enabled);
    }

    @Override
    public List<GroupResponse> getUserGroups(String token, String userId) {
        return keycloakService.getUserGroups(token, userId);
    }
}

