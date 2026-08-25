package org.chud.springuniapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.repository.UserRepository;

public class NotAdminValidator implements ConstraintValidator<NotAdmin, Long> {

    private final UserRepository userRepository;

    public NotAdminValidator(UserRepository userRepository) {
        this.userRepository =  userRepository;
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        //should pass on null, @NotNull constraint's work to validate there
        if (userId == null) {
            return true;
        }

        return userRepository.findRoleNameByUserId(userId).
                map(role -> role != RoleName.ADMIN)
                .orElse(true);
    }
}
