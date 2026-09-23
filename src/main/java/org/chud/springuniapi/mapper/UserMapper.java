package org.chud.springuniapi.mapper;

import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.dto.response.UserSoftDeleteResponse;
import org.chud.springuniapi.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    //The course list is already filtered and ordered by the repository query,
    //the mapper only copies it into the response
    @Mapping(target = "courses", source = "courseSummaries")
    UserResponse toResponse(User user, List<CourseSummaryResponse> courseSummaries);

    @Mapping(target = "courses", source = "courseSummaries")
    @Mapping(target = "isDeleted", source = "user.deleted")
    UserSoftDeleteResponse toResponseWithSoftDelete(User user,
        List<CourseSummaryResponse> courseSummaries);
}
