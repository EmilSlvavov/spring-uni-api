package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.CreateDepartmentRequest;
import org.chud.springuniapi.dto.request.ReplaceContactsRequest;
import org.chud.springuniapi.dto.request.UpdateDepartmentRequest;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;

import java.util.List;

public interface IDepartmentService {

    //deleted = null means the ?deleted param was absent, so the courses are not filtered
    List<DepartmentResponse> findAll(Boolean deleted);

    DepartmentResponse findById(Long id, Boolean deleted);

    List<DepartmentSoftDeleteResponse> findAllBySoftDeleted(boolean isDeleted);

    DepartmentResponse create(CreateDepartmentRequest request);

    DepartmentResponse update(Long id, UpdateDepartmentRequest request);

    void delete(Long id);

    DepartmentSoftDeleteResponse softDelete(Long id);

    DepartmentSoftDeleteResponse restoreSoftDelete(Long id);

    DepartmentResponse replaceContacts(Long id, ReplaceContactsRequest request);
}
