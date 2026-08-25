package org.chud.springuniapi.config;

import org.chud.springuniapi.entity.Role;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.repository.RoleRepository;
import org.chud.springuniapi.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("!test")
public class DevDataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public DevDataSeeder(RoleRepository roleRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         @Value("${user.seed.password}") String seedPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run( @NonNull ApplicationArguments args) throws Exception {

        //Cannot create a user without a role, so we create roles first
        Arrays.stream(RoleName.values())
                .filter(role -> roleRepository.findByRoleName(role).isEmpty())
                .forEach(role -> roleRepository.save(new Role(role)));

        if(userRepository.count() > 0){
            return;
        }

        Arrays.stream(RoleName.values()).forEach(roleName -> {
            Role role = roleRepository.findByRoleName(roleName).orElseThrow();
            userRepository.save(new User(
                    role.getRoleName().name().toLowerCase() + " user",
                    role.getRoleName().name().toLowerCase() + "@email.test",
                    passwordEncoder.encode(seedPassword),
                    role
            ));
        });
    }
}
