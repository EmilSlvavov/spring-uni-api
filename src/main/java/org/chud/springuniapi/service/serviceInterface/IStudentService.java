package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.request.UpdateStudentProfileRequest;
import org.chud.springuniapi.dto.request.UpdateStudentRequest;
import org.chud.springuniapi.dto.response.StudentDisplayResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.dto.response.StudentSoftDeleteResponse;

import java.util.List;

public interface IStudentService {

    //deleted = null means the ?deleted param was absent, so the courses are not filtered
    List<StudentResponse> findAll(Boolean deleted);

    StudentResponse findById(Long id, Boolean deleted);

    List<StudentDisplayResponse> findDisplayLabels();

    List<StudentSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    StudentResponse create(CreateStudentRequest request);

    StudentResponse update(Long id, UpdateStudentRequest request);

    StudentResponse updateProfile(Long id, UpdateStudentProfileRequest request);

    StudentResponse enroll(Long studentId, Long courseId);

    StudentResponse withdraw(Long studentId, Long courseId);

    void delete(Long id);

    StudentSoftDeleteResponse softDelete(Long id);

    StudentSoftDeleteResponse restoreSoftDelete(Long id);
}
