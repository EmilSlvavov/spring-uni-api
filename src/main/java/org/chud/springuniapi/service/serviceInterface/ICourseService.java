package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;

import java.util.List;

public interface ICourseService {

    //deleted = null means the ?deleted param was absent, so the students are not filtered
    List<CourseResponse> findAll(Boolean deleted);

    CourseResponse findById(Long id, Boolean deleted);

    //the closed projection endpoint
    List<CourseListItemResponse> findSummariesByDepartment(Long departmentId);

    List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted);

    List<CourseSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    CourseResponse createOnline(CreateOnlineCourseRequest request);

    CourseResponse createOnsite(CreateOnsiteCourseRequest request);

    CourseResponse update(Long id, UpdateCourseRequest request);

    void delete(Long id);

    CourseSoftDeleteResponse softDelete(Long id);

    CourseSoftDeleteResponse restoreSoftDelete(Long id);
}
