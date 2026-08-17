package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.ContactInfoResponse;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;
import org.chud.springuniapi.entity.ContactInfo;
import org.chud.springuniapi.entity.Course;
import org.chud.springuniapi.entity.Department;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(config = CentralMapperConfig.class)
public interface DepartmentMapper {
                                                        //This property is called from the named method
    @Mapping(target = "contacts", source = "contacts", qualifiedByName = "sortedContacts")
    @Mapping(target = "courses", source = "courses", qualifiedByName = "filteredCourses")
    DepartmentResponse toResponse(Department department, @Context Boolean deleted);

    default  DepartmentResponse toResponse(Department department) {
        return toResponse(department, null);
    }

    ContactInfoResponse toContactResponse(ContactInfo contactInfo);

    CourseSummaryResponse toCourseSummary(Course course);

    @Mapping(target = "contacts", source = "contacts", qualifiedByName = "sortedContacts")
    @Mapping(target = "isDeleted", source = "deleted")
    DepartmentSoftDeleteResponse toResponseWithSoftDelete(Department department);

    //Named tells you to use this specific method for this property
    @Named("sortedContacts")
    default List<ContactInfoResponse> sortedContacts(Set<ContactInfo> contacts) {
        if (contacts == null) {
            return List.of();
        }

        return contacts
                .stream()
                .map(this::toContactResponse)
                .sorted(Comparator.comparing(ContactInfoResponse::type)
                        .thenComparing(ContactInfoResponse::value))
                .toList();
    }

    //filter by soft deleted. @Context is used to pass the Boolean value without it being part of the entity
    @Named("filteredCourses")
    default List<CourseSummaryResponse> filteredCourses(Set<Course> courses, @Context Boolean deleted) {
        if (courses == null) {
            return List.of();
        }

        return courses.stream()
            .filter(course -> deleted == null || course.isDeleted() == deleted)
            .map(this::toCourseSummary)
            .sorted(Comparator.comparing(CourseSummaryResponse::id))
            .toList();
    }
}
