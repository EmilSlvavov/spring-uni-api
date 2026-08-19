package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;
import org.chud.springuniapi.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public interface StudentMapper {

    //The course list is already filtered and ordered by the repository query,
    //the mapper only copies it into the response
    @Mapping(target = "courses", source = "courseSummaries")
    StudentResponse toResponse(Student student, List<CourseSummaryResponse> courseSummaries);

    @Mapping(target = "courses", source = "courseSummaries")
    @Mapping(target = "isDeleted", source = "student.deleted")
    StudentSoftDeleteResponse toResponseWithSoftDelete(Student student,
        List<CourseSummaryResponse> courseSummaries);
}
