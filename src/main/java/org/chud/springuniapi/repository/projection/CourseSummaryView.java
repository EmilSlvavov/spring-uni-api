package org.chud.springuniapi.repository.projection;

//Methods have to match exact field name
public interface CourseSummaryView extends DynamicCourseProjectionMarker {
    Long getId();
    String getName();
    DepartmentNameView getDepartment();

    //nested interface to get both views
    interface DepartmentNameView {
        String getName();
    }
}
