package platformer.service.rest.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String username;
    private String password;
    private List<String> roles;
}
