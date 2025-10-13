package com.example.authservice.model.entity;

import com.example.authservice.model.enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "fin"),
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 7)
    private String fin;

    @Column(nullable = false, length = 120)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default // <-- Bu sətir əlavə edildi
    @Column(nullable = false)
    private boolean enabled = false; // Hesabın aktiv olub-olmadığını göstərir (OTP ilə təsdiqlənir)

    @Builder.Default // <-- Bu sətir əlavə edildi
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return fin; // Authentication üçün FIN-i username kimi istifadə edirik
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // `UserDetails` interfeysinin `getUsername()` metodunun qaytardığı dəyər ilə
    // bizim `username` sahəmizin adı eyni olduğu üçün qarışıqlıq yarana bilər.
    // Metodu `getFin()` adlandırıb, `getUsername()` isə `fin` dəyərini qaytara bilərik.
    // Yuxarıda `getUsername()` metodunu `fin` olaraq override etdim.
    // Əgər həqiqi username sahəsini istifadə etmək istəyirsinizsə, SecurityConfig-i də uyğunlaşdırmalısınız.
    public String getActualUsername() {
        return username; // Bu metod vasitəsilə əsl username-ə çata bilərik
    }
}