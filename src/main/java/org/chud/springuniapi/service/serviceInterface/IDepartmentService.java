package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.request.CreateDepartmentRequest;
import org.chud.springuniapi.dto.request.ReplaceContactsRequest;
import org.chud.springuniapi.dto.request.UpdateDepartmentRequest;
import org.chud.springuniapi.dto.response.DepartmentResponse;
import org.chud.springuniapi.dto.response.DepartmentSoftDeleteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IDepartmentService {

    Page<DepartmentResponse> search(Boolean deleted, Pageable pageable, String q);

    DepartmentResponse findById(Long id, Boolean deleted);

    List<DepartmentSoftDeleteResponse> findAllBySoftDeleted(Boolean isDeleted);

    DepartmentResponse create(CreateDepartmentRequest request);

    DepartmentResponse update(Long id, UpdateDepartmentRequest request);

    void delete(Long id);

    DepartmentSoftDeleteResponse softDelete(Long id);

    DepartmentSoftDeleteResponse restoreSoftDelete(Long id);

    DepartmentResponse replaceContacts(Long id, ReplaceContactsRequest request);
}
