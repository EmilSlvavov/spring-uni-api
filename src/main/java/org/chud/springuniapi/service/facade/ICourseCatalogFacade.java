package org.chud.springuniapi.service.facade;

import java.util.List;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourseCatalogFacade {

    List<CourseListItemResponse> findSummariesByDepartment(Long departmentId);

    Page<CourseResponse> search(String q, CourseType type, Long departmentId, Long lecturerId,
                                Boolean deleted, Pageable pageable);

    CourseResponse createOnline(CreateOnlineCourseRequest request, Long creatorId);

    CourseResponse createOnsite(CreateOnsiteCourseRequest request, Long creatorId);
}
