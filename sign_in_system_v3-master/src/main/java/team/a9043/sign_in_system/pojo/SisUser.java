package team.a9043.sign_in_system.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SisUser {
    private String suId;

    private String suAuthoritiesStr;

    private String suName;

    private String suOpenid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String suPassword;

    private String type;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer suiLackNum;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer suiCozLackNum;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisUserInfo sisUserInfo;

    public List<GrantedAuthority> getSuAuthorities() {
        if (null == suAuthoritiesStr) {
            return null;
        }
        return Arrays
            .stream(suAuthoritiesStr.split(","))
            .map(String::trim)
            .map(String::toUpperCase)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    public void setSuAuthorities(List<GrantedAuthority> suAuthorityList) {
        this.suAuthoritiesStr = String.join(",",
            suAuthorityList.stream().map(GrantedAuthority::getAuthority).toArray(String[]::new));
    }

    @Override
    public int hashCode() {
        return ("SisUser_" + suId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SisUser))
            return false;
        return this.suId.equals(((SisUser) obj).suId);
    }
}