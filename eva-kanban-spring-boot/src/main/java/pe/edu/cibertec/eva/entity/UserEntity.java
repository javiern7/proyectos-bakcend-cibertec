package pe.edu.cibertec.eva.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=60)
    private String username;
    @Column(nullable=false, length=255)
    private String email;
    @Column(nullable=false, length=255)
    private String password;
    @Column(nullable=false, length=20)
    private String role;
    @Column(nullable=false)
    private Boolean enabled = true;

    public boolean isAdmin() {
        if (role == null) return false;
        String r = role.trim().toUpperCase();
        return "ADMIN".equals(r) || "ROLE_ADMIN".equals(r);
    }
    
}
