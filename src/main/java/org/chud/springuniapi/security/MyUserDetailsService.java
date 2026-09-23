package org.chud.springuniapi.security;

import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final IUserServiceInternal userService;

    public MyUserDetailsService(IUserServiceInternal userService) {
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userService.loadByEmail(email)
                .map(this::toPrincipal)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with such email does not exists" + email));
    }

    @Transactional(readOnly = true)
    public Optional<MyUserDetails> loadById(Long userId) {
        return userService.loadById(userId).map(this::toPrincipal);
    }

    private MyUserDetails toPrincipal(User user) {
        return new MyUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().getAuthorityString())),
                !user.isDeleted()
        );
    }
}
