package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Student;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(config = CentralMapperConfig.class)
public interface StudentMapper {

    //MapStruct can do Set -> List conversions, but it will not be ordered due to
    //HashSet. It will vary between request. sortedCourses is purely for sorting
    @Mapping(target = "courses", source = "courses", qualifiedByName = "filteredCourses")
    StudentResponse toResponse(Student student, @Context Boolean deleted);

    default StudentResponse toResponse(Student student) {
        return toResponse(student, null);
    }

    @Mapping(target = "courses", source = "courses", qualifiedByName = "sortedCourses")
    @Mapping(target = "isDeleted", source = "deleted")
    StudentSoftDeleteResponse toResponseWithSoftDelete(Student student);

    CourseSummaryResponse toCourseSummary(Course course);

    @Named("filteredCourses")
    default List<CourseSummaryResponse> filteredCourses(Set<Course> courses,
        @Context Boolean deleted) {
        if (courses == null) {
            return List.of();
        }
        return courses.stream()
            .filter(course -> deleted == null || course.isDeleted() == deleted)
            .map(this::toCourseSummary)
            .sorted(Comparator.comparing(CourseSummaryResponse::id))
            .toList();
    }

    @Named("sortedCourses")
    default List<CourseSummaryResponse> sortedCourses(Set<Course> courses) {
        return filteredCourses(courses, null);
    }
}
