package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.response.UserResponse;


public interface IEnrollmentFacade {

    UserResponse enroll(Long userId, Long courseId);

    UserResponse withdraw(Long userId, Long courseId);
}
