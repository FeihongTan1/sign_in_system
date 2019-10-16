package team.a9043.sign_in_system.security.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team.a9043.sign_in_system.pojo.SisUser;

import java.util.Collection;

public class SecurityUserDetails implements UserDetails {
    private SisUser sisUser;

    public SecurityUserDetails(SisUser sisUser) {
        this.sisUser = sisUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return sisUser.getSuAuthorities();
    }

    @Override
    public String getPassword() {
        return sisUser.getSuPassword();
    }

    @Override
    public String getUsername() {
        return sisUser.getSuId();
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
        return true;
    }

    public SisUser getSisUser() {
        return sisUser;
    }

    public void setSisUser(SisUser sisUser) {
        this.sisUser = sisUser;
    }
}
