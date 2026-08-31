package org.chud.springuniapi.service;

import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.dto.response.UserSummaryResponse;
import org.chud.springuniapi.entity.*;
import org.chud.springuniapi.exception.BusinessRuleViolationException;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.mapper.CourseMapper;
import org.chud.springuniapi.repository.CourseRepository;
import org.chud.springuniapi.repository.projection.CourseSummaryView;
import org.chud.springuniapi.repository.projection.UserSummaryRow;
import org.chud.springuniapi.service.serviceInterface.ICourseService;
import org.chud.springuniapi.service.serviceInterface.internal.ICourseServiceInternal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

//The only class allowed to touch CourseRepository.
@Service
@Transactional(readOnly = true)
public class CourseServiceImpl implements ICourseService, ICourseServiceInternal {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public List<CourseResponse> findAll(Boolean deleted) {
        List<Course> courses = courseRepository.findAllWithDepartment();
        Map<Long, List<UserSummaryResponse>> usersByCourse = listUsersByCourseId(courses, deleted);

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, usersOfForMultipleCourses(usersByCourse, course)))
                .toList();
    }

    @Override
    public CourseResponse findById(Long id, Boolean deleted) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        return courseMapper.toResponse(course, usersOfForSingleCourse(course, deleted));
    }

    @Override
    public List<CourseSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted){
        return courseRepository
            .findCourseByDeleted(isDeleted)
            .stream()
            .map(courseMapper::toResponseWithSoftDelete)
            .toList();
    }

    @Override
    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setName(request.name()); // no save() flush() will save changes
        return courseMapper.toResponse(course, usersOfForSingleCourse(course, null));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        for (User user : Set.copyOf(course.getUsers())) {
            user.withdraw(course);
        }

        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseSoftDeleteResponse softDelete(Long id) {
        Course course = courseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setDeleted(true);
        return courseMapper.toResponseWithSoftDelete(course);

    }

    @Override
    @Transactional
    public CourseSoftDeleteResponse restoreSoftDelete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        course.setDeleted(false);
        return courseMapper.toResponseWithSoftDelete(course);

    }


    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Course load(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    //The pessimistic read lock and the question of what makes a course enrollable both
    //live here, in the aggregate that owns the answer, instead of at the call site.
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Course loadForEnrollment(Long id) {
        Course course = courseRepository.findWithLockById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if (course.isDeleted()) {
            throw new BusinessRuleViolationException(
                "cannot enroll in a deleted course");
        }

        return course;
    }

    //The department existence check that used to sit here moved to
    //CourseCatalogFacadeImpl, which is allowed to ask the Department aggregate.
    @Override
    public List<CourseListItemResponse> findSummariesByDepartment(Long departmentId) {
        List<CourseSummaryView> views =
            courseRepository.findByDepartmentId(departmentId, CourseSummaryView.class);

        return views.stream().map(view -> new CourseListItemResponse(
                        view.getId(),
                        view.getName(),
                        view.getDepartment().getName()))
                .toList();
    }

    @Override
    public List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted) {
        List<Course> courses = courseRepository.findByDepartmentId(departmentId);

        Map<Long, List<UserSummaryResponse>> usersByCourse = listUsersByCourseId(courses, deleted);

        return courses.stream()
                .map(course -> courseMapper.toResponse(course, usersOfForMultipleCourses(usersByCourse, course)))
                .toList();
    }

    //The Department arrives resolved and locked from the facade.
    @Override
    @Transactional
    public CourseResponse createOnline(CreateOnlineCourseRequest request, Department department) {
        //a fresh course has no users yet, so there is nothing to query for
        return courseMapper.toResponse(courseRepository.save(
                new OnlineCourse(request.name(), department, request.meetingUrl())), List.of());
    }

    @Override
    @Transactional
    public CourseResponse createOnsite(CreateOnsiteCourseRequest request, Department department) {
        return courseMapper.toResponse(courseRepository.save(
                new OnsiteCourse(request.name(), department, request.roomNumber())), List.of());
    }

    //one query for the whole list instead of one per course
    private Map<Long, List<UserSummaryResponse>> listUsersByCourseId(List<Course> courses, Boolean deleted) {
        List<Long> ids = courses.stream().map(Course::getId).toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        return UserSummaryRow.groupByOwner(
                courseRepository.findUserSummariesByCourseIds(ids, deleted));
    }

    private List<UserSummaryResponse> usersOfForSingleCourse(Course course, Boolean deleted) {
        return usersOfForMultipleCourses(listUsersByCourseId(List.of(course), deleted), course);
    }

    private List<UserSummaryResponse> usersOfForMultipleCourses(Map<Long, List<UserSummaryResponse>> usersByCourse,
        Course course) {

        return usersByCourse.getOrDefault(course.getId(), List.of());
    }
}
