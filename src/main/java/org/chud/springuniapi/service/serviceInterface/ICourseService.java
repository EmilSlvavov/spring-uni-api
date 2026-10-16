package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.UpdateCourseRequest;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


//Single aggregate operations only. Creating a course and the by-department reads
//both need the Department aggregate, so they moved to ICourseCatalogFacade.
public interface ICourseService {

    CourseResponse findById(Long id, Boolean deleted);

    Page<CourseSoftDeleteResponse> findAllBySoftDeleted(Boolean deleted, Long lecturerId, Pageable pageable);

    CourseResponse update(Long id, UpdateCourseRequest request);

    void delete(Long id);

    CourseSoftDeleteResponse softDelete(Long id);

    CourseSoftDeleteResponse restoreSoftDelete(Long id);
}
