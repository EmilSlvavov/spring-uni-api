package org.chud.springuniapi.service.serviceInterface.internal;

import java.util.List;
import org.chud.springuniapi.dto.request.CreateOnlineCourseRequest;
import org.chud.springuniapi.dto.request.CreateOnsiteCourseRequest;
import org.chud.springuniapi.dto.response.CourseListItemResponse;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;
import org.chud.springuniapi.entity.User;
import org.chud.springuniapi.enums.CourseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

//separate from ICourseService since controller injects ICourseService
//you do not want to give it access to entity objects, only dtos
public interface ICourseServiceInternal {

    Page<CourseResponse> search(String q, CourseType type, Long departmentId, Long lecturerId,
                                Boolean deleted, Pageable pageable);

    Course load(Long id);

    Course loadForEnrollment(Long id);

    boolean isLecturedBy(Long courseId, Long userId);

    void unassignLecturer(Long lecturerId);

    List<CourseListItemResponse> findSummariesByDepartment(Long departmentId);

    CourseResponse createOnline(CreateOnlineCourseRequest request, Department department, User lecturer);

    CourseResponse createOnsite(CreateOnsiteCourseRequest request, Department department, User lecturer);
}
