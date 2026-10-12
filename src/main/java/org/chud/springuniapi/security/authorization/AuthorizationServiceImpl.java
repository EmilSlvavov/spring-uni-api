package org.chud.springuniapi.security.authorization;

import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.service.serviceInterface.internal.ICourseServiceInternal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("authorizationService")
public class AuthorizationServiceImpl implements AuthorizationService{

    private final ICourseServiceInternal courseService;

    public AuthorizationServiceImpl(ICourseServiceInternal courseService) {
        this.courseService = courseService;
    }

    @Override
    public boolean isAccessingSelf(Long id, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails principal)) {
            return false;
        }
        return Objects.equals(id, principal.id());
    }

    @Override
    public boolean isCourseOwner(Long courseId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails principal)) {
            return false;
        }

        return courseService.isLecturedBy(courseId, principal.id());
    }
}
