package cg.homelab.gateway.models.Dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
