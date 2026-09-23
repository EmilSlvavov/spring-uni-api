package org.chud.springuniapi.service.serviceInterface.internal;

import java.util.List;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;

//separate from ICourseService since controller injects ICourseService
//you do not want to give it access to entity objects, only dtos
public interface ICourseServiceInternal {

    Course load(Long id);

    Course loadForEnrollment(Long id);

    List<CourseListItemResponse> findSummariesByDepartment(Long departmentId);

    List<CourseResponse> findByDepartment(Long departmentId, Boolean deleted);

    CourseResponse createOnline(CreateOnlineCourseRequest request, Department department);

    CourseResponse createOnsite(CreateOnsiteCourseRequest request, Department department);
}
