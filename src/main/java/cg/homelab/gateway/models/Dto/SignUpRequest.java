package cg.homelab.gateway.models.Dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String password;
    private String repeatPassword;
    private String role;
}
