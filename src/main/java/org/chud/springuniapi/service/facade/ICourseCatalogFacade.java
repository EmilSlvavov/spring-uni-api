package org.chud.springuniapi.service.facade;

import java.util.List;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;

public interface ICourseCatalogFacade {

    List<CourseListItemResponse> findSummariesByDepartment(Long departmentId);

    List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted);

    CourseResponse createOnline(CreateOnlineCourseRequest request);

    CourseResponse createOnsite(CreateOnsiteCourseRequest request);
}
