package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.application_events.event.EnrollUserEvent;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.service.serviceInterface.internal.ICourseServiceInternal;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//removes CourseRepository from UserServiceImpl
@Service
public class EnrollmentFacadeImpl implements IEnrollmentFacade {

    private final IUserServiceInternal userService;
    private final ICourseServiceInternal courseService;
    private final ApplicationEventPublisher eventPublisher;

    public EnrollmentFacadeImpl(
        IUserServiceInternal userService,
        ICourseServiceInternal courseService,
        ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.courseService = courseService;
        this.eventPublisher = eventPublisher;
    }

    //The transaction is opened here now instead of in UserServiceImpl. Both entities
    //have to be managed by the same persistence context for user.enroll(course) to be
    //picked up by dirty checking.
    @Override
    @Transactional
    public UserResponse enroll(Long userId, Long courseId) {
        User user = userService.loadWithCourses(userId);
        Course course = courseService.loadForEnrollment(courseId);

        user.enroll(course);

        eventPublisher.publishEvent(new EnrollUserEvent(userId, courseId, course.getName()));

        return userService.describe(user);
    }

    @Override
    @Transactional
    public UserResponse withdraw(Long userId, Long courseId) {
        User user = userService.loadWithCourses(userId);
        Course course = courseService.load(courseId);

        user.withdraw(course);

        return userService.describe(user);
    }
}
