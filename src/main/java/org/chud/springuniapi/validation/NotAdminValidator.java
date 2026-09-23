package org.chud.springuniapi.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.chud.springuniapi.enums.RoleName;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;

public class NotAdminValidator implements ConstraintValidator<NotAdmin, Long> {

    private final IUserServiceInternal userService;

    public NotAdminValidator(IUserServiceInternal userService) {
        this.userService =  userService;
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        //should pass on null, @NotNull constraint's work to validate there
        if (userId == null) {
            return true;
        }

        return userService.roleOf(userId).
                map(role -> role != RoleName.ADMIN)
                .orElse(true);
    }
}
