package org.chud.springuniapi.mapper;

import java.util.Comparator;
import java.util.Set;
import org.chud.springuniapi.dto.response.CourseResponse;
import org.chud.springuniapi.dto.response.CourseSoftDeleteResponse;
import org.chud.springuniapi.dto.response.StudentSummaryResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.OnlineCourse;
import org.chud.springuniapi.entity.OnsiteCourse;
import org.chud.springuniapi.entity.Student;
import org.hibernate.Hibernate;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public interface CourseMapper {

    //toResponse mapper with switch case for both online and onsite courses
    default CourseResponse toResponse(Course course, @Context Boolean deleted) {
        return switch (Hibernate.unproxy(course)) {
            case OnlineCourse online -> toResponse(online, deleted);
            case OnsiteCourse onsite -> toResponse(onsite, deleted);
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

    default CourseResponse toResponse(Course course) {
        return toResponse(course, null);
    }

    StudentSummaryResponse toStudentSummary(Student student);

    //toResponse for online
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "type", constant = "ONLINE")
    @Mapping(target = "roomNumber", ignore = true)
    @Mapping(target = "students", source = "students", qualifiedByName = "filteredStudents")
    CourseResponse toResponse(OnlineCourse course, @Context Boolean deleted);

    //toResponse for onsite
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "type", constant = "ONSITE")
    @Mapping(target = "meetingUrl", ignore = true)
    @Mapping(target = "students", source = "students", qualifiedByName = "filteredStudents")
    CourseResponse toResponse(OnsiteCourse course, @Context Boolean deleted);



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


    @Named("filteredStudents")
    default List<StudentSummaryResponse> filteredStudents(Set<Student> students, @Context Boolean deleted) {
        if (students == null) {
            return List.of();
        }
        return students.stream()
            .filter(student -> deleted == null || student.isDeleted() == deleted)
            .map(this::toStudentSummary)
            .sorted(Comparator.comparing(StudentSummaryResponse::id))
            .toList();
    }
}
