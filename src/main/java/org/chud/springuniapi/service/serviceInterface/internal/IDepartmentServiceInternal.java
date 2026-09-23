package org.chud.springuniapi.service.serviceInterface.internal;

import org.chud.springuniapi.entity.Department;

public interface IDepartmentServiceInternal {

    //Takes the pessimistic read lock a course creation needs.
    Department loadForCourseCreation(Long id);

    //Throws ResourceNotFoundException when the department is not there. Deliberately
    //not a boolean - the caller should not get to invent its own 404 message.
    void requireExists(Long id);
}
