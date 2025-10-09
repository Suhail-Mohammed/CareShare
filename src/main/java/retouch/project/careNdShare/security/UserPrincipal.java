package retouch.project.careNdShare.security;


import retouch.project.careNdShare.entity.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserPrincipal implements UserDetails {
    private Long id;
    private String email;
    private String password;
    private boolean isAdmin;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password,
                         Collection<? extends GrantedAuthority> authorities, boolean isAdmin) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.isAdmin = isAdmin;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());

        // Add ADMIN role if user is admin
        if (user.isAdmin()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.isAdmin()
        );
    }

    // Getters
    public Long getId() { return id; }
    public String getEmail() { return email; }

    @Override
    public String getUsername() { return email; }
    @Override
    public String getPassword() { return password; }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
    public boolean isAdmin() { return isAdmin; }
}

