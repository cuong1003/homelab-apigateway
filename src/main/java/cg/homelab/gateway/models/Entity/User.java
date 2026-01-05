package cg.homelab.gateway.models.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
@Table("users")
@Data
public class User {
    @Id
    private Integer id;
    private String username;
    private String password;
    private String role;
}
