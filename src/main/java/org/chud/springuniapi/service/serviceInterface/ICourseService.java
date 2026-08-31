package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;

import java.util.List;

//Single aggregate operations only. Creating a course and the by-department reads
//both need the Department aggregate, so they moved to ICourseCatalogFacade.
public interface ICourseService {

    //enabled = null means the ?enabled param was absent, so the users are not filtered
    List<CourseResponse> findAll(Boolean deleted);

    CourseResponse findById(Long id, Boolean deleted);

    List<CourseSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    CourseResponse update(Long id, UpdateCourseRequest request);

    void delete(Long id);

    CourseSoftDeleteResponse softDelete(Long id);

    CourseSoftDeleteResponse restoreSoftDelete(Long id);
}
