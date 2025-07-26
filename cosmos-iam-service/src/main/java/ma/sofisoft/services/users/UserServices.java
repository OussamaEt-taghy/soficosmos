package ma.sofisoft.services.users;
import ma.sofisoft.dtos.userGroup.GroupResponse;
import ma.sofisoft.dtos.users.CreateUserRequest;
import ma.sofisoft.dtos.users.UpdateUserRequest;
import ma.sofisoft.dtos.users.UserResponse;

import java.util.List;

public interface UserServices {
    String createUser(String token, CreateUserRequest request);
    void updateUserInfo(String token, String userId, UpdateUserRequest request);
    void updateUserPassword(String token, String username, String newPassword);
    void deleteUser(String token, String userId);
    List<UserResponse> getUsers(String token);
    UserResponse getUserById(String token, String userId);
    void enableUser(String token, String userId, boolean enabled);
    List<GroupResponse> getUserGroups(String token, String userId);
}
