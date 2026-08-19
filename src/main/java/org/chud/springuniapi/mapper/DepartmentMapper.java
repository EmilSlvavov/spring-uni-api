package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.ContactInfoResponse;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;
import org.chud.springuniapi.entity.ContactInfo;
import org.chud.springuniapi.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(config = CentralMapperConfig.class)
public interface DepartmentMapper {

    //courses come in already filtered by the repository, contacts are just sorted
    @Mapping(target = "contacts", source = "department.contacts", qualifiedByName = "sortedContacts")
    @Mapping(target = "courses", source = "courseSummaries")
    DepartmentResponse toResponse(Department department, List<CourseSummaryResponse> courseSummaries);

    ContactInfoResponse toContactResponse(ContactInfo contactInfo);

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
}
