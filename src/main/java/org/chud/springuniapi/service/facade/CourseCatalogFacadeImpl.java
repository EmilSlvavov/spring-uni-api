package org.chud.springuniapi.service.facade;

import java.util.List;
import java.util.Optional;

import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.service.serviceInterface.internal.ICourseServiceInternal;
import org.chud.springuniapi.service.serviceInterface.internal.IDepartmentServiceInternal;
import org.chud.springuniapi.service.serviceInterface.internal.IUserServiceInternal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseCatalogFacadeImpl implements ICourseCatalogFacade {

    private final ICourseServiceInternal courseService;
    private final IDepartmentServiceInternal departmentService;
    private final IUserServiceInternal userServiceInternal;

    public CourseCatalogFacadeImpl(
            ICourseServiceInternal courseService,
            IDepartmentServiceInternal departmentService, IUserServiceInternal userServiceInternal) {
        this.courseService = courseService;
        this.departmentService = departmentService;
        this.userServiceInternal = userServiceInternal;
    }

    @Override
    public List<CourseListItemResponse> findSummariesByDepartment(Long departmentId) {
        List<CourseListItemResponse> summaries =
            courseService.findSummariesByDepartment(departmentId);

        if (summaries.isEmpty()) {
            departmentService.requireExists(departmentId);
        }

        return summaries;
    }

    @Override
    public List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted) {
        List<CourseResponse> courses = courseService.findByDepartment(departmentId, deleted);

        if (courses.isEmpty()) {
            departmentService.requireExists(departmentId);
        }

        return courses;
    }

    @Override
    @Transactional
    public CourseResponse createOnline(CreateOnlineCourseRequest request, Long creatorId) {
        Department department = departmentService.loadForCourseCreation(request.departmentId());

        User creator = userServiceInternal.loadById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));

        User lecturer = creator.isAdmin() ? null : creator;

        return courseService.createOnline(request, department, lecturer);
    }

    @Override
    @Transactional
    public CourseResponse createOnsite(CreateOnsiteCourseRequest request, Long creatorId) {
        Department department = departmentService.loadForCourseCreation(request.departmentId());

        User creator = userServiceInternal.loadById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", creatorId));

        User lecturer = creator.isAdmin() ? null : creator;

        return courseService.createOnsite(request, department, lecturer);
    }
}
