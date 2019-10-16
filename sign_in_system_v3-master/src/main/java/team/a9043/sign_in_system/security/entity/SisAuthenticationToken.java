package team.a9043.sign_in_system.security.entity;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import team.a9043.sign_in_system.pojo.SisUser;

import java.util.Collection;

public class SisAuthenticationToken extends AbstractAuthenticationToken {
    private SisUser sisUser;

    public SisAuthenticationToken(SisUser sisUser) {
        this(sisUser.getSuAuthorities());
        this.sisUser = sisUser;
    }

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for
     *                    the principal
     *                    represented by this authentication object.
     */
    private SisAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return sisUser.getSuId();
    }

    public SisUser getSisUser() {
        return sisUser;
    }
}
