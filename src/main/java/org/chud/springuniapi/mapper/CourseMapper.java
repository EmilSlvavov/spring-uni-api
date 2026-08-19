package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.dto.response.StudentSummaryResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public interface CourseMapper {

    //toResponse mapper with switch case for both online and onsite courses
    default CourseResponse toResponse(Course course, List<StudentSummaryResponse> studentSummaries) {
        return switch (Hibernate.unproxy(course)) {
            case OnlineCourse online -> toResponse(online, studentSummaries);
            case OnsiteCourse onsite -> toResponse(onsite, studentSummaries);
            default -> throw new IllegalStateException(
                    "Unmapped course subtype: " + course.getClass().getName());
        };
    }

    default CourseSoftDeleteResponse toResponseWithSoftDelete(Course course) {
        return switch (Hibernate.unproxy(course)) {
            case OnlineCourse online -> toResponseWithSoftDelete(online);
            case OnsiteCourse onsite -> toResponseWithSoftDelete(onsite);
            default -> throw new IllegalStateException(
                "Unmapped course subtype: " + course.getClass().getName());
        };
    }

    //toResponse for online, the student list is already filtered by the repository
    @Mapping(target = "departmentId", source = "course.department.id")
    @Mapping(target = "departmentName", source = "course.department.name")
    @Mapping(target = "type", constant = "ONLINE")
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "students", source = "studentSummaries")
    CourseResponse toResponse(OnlineCourse course, List<StudentSummaryResponse> studentSummaries);

    //toResponse for onsite
    @Mapping(target = "departmentId", source = "course.department.id")
    @Mapping(target = "departmentName", source = "course.department.name")
    @Mapping(target = "type", constant = "ONSITE")
    @Mapping(target = "meetingUrl", ignore = true)
    @Mapping(target = "students", source = "studentSummaries")
    CourseResponse toResponse(OnsiteCourse course, List<StudentSummaryResponse> studentSummaries);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "type", constant = "ONLINE")
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "isDeleted", source = "deleted")
    CourseSoftDeleteResponse toResponseWithSoftDelete(OnlineCourse course);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "type", constant = "ONSITE")
    @Mapping(target = "meetingUrl", ignore = true)
    @Mapping(target = "isDeleted", source = "deleted")
    CourseSoftDeleteResponse toResponseWithSoftDelete(OnsiteCourse course);
}
