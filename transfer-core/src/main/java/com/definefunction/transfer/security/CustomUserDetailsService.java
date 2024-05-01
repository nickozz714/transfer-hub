package com.definefunction.transfer.security;

import com.definefunction.transfer.model.Principal;
import com.definefunction.transfer.model.pojo.AuthenticationRole;
import com.definefunction.transfer.model.pojo.Role;
import com.definefunction.transfer.service.PrincipalService;
import org.apache.camel.language.simple.Simple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PrincipalService principalService;

    private AuthenticationRole authenticationRole;

    public AuthenticationRole getAuthenticationRole() {
        return authenticationRole;
    }

    public void setAuthenticationRole(AuthenticationRole authenticationRole) {
        this.authenticationRole = authenticationRole;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Principal principal = principalService.findPrincipalByUsername(username).orElseThrow(() -> new UsernameNotFoundException("No user exists with username: "+username));
        switch (principal.getAuthenticationRole()) {
            case ADMIN:
                SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority(AuthenticationRole.ADMIN.toString());
                Collection<GrantedAuthority> adminAuthorities = new ArrayList<>();
                adminAuthorities.add(adminAuthority);
                return new User(principal.getUsername(), principal.getPassword(), adminAuthorities);
            case PRINCIPAL:
                SimpleGrantedAuthority principalAuthority = new SimpleGrantedAuthority(AuthenticationRole.PRINCIPAL.toString());
                Collection<GrantedAuthority> principalAuthorities = new ArrayList<>();
                principalAuthorities.add(principalAuthority);
                return new User(principal.getUsername(), principal.getPassword(), principalAuthorities);
            default:
                return null;
        }
    }
}
