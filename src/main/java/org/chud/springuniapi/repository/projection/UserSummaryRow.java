package org.chud.springuniapi.repository.projection;

import org.chud.springuniapi.dto.response.UserSummaryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Same idea as CourseSummaryRow, ownerId is the id of the course the user is enrolled in
public record UserSummaryRow(Long ownerId, Long userId, String userName) {

    public static Map<Long, List<UserSummaryResponse>> groupByOwner(List<UserSummaryRow> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                UserSummaryRow::ownerId,
                Collectors.mapping(
                        row -> new UserSummaryResponse(row.userId(), row.userName()),
                        Collectors.toList())));
    }
}
