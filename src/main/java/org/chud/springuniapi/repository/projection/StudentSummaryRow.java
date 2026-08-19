package org.chud.springuniapi.repository.projection;

import org.chud.springuniapi.dto.response.StudentSummaryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//Same idea as CourseSummaryRow, ownerId is the id of the course the student is enrolled in
public record StudentSummaryRow(Long ownerId, Long studentId, String studentName) {

    public static Map<Long, List<StudentSummaryResponse>> groupByOwner(List<StudentSummaryRow> rows) {
        return rows.stream().collect(Collectors.groupingBy(
                StudentSummaryRow::ownerId,
                Collectors.mapping(
                        row -> new StudentSummaryResponse(row.studentId(), row.studentName()),
                        Collectors.toList())));
    }
}
