package org.chud.springuniapi.dto.response;

//used for the closed projection, no from() since its source is not an entity
//mapping occurs in the service
public record CourseListItemResponse(Long id, String name, String departmentName) { }
