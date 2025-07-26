package ma.sofisoft.dtos.users;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
}
