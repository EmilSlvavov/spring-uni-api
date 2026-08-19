package org.chud.springuniapi.repository.projection;

import org.chud.springuniapi.dto.response.CourseSummaryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//ownerId is crucial. It can be both for student and department
//Without it one query could only ever serve one owner and a list endpoint would be back to N+1.
public record CourseSummaryRow(Long ownerId, Long courseId, String courseName) {

    //use a list of CourseSummaryRows to make a map which uses the ownerId for key and the
    //value is the courseId and courseName turned mapped to a CourseSummaryResponse creating a map of
    //owners who have courses
    public static Map<Long, List<CourseSummaryResponse>> groupByOwner(List<CourseSummaryRow> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                CourseSummaryRow::ownerId,
                Collectors.mapping(
                        row -> new CourseSummaryResponse(row.courseId(), row.courseName()),
                        Collectors.toList())));
    }
}
