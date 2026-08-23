package org.chud.springuniapi.application_events.listener;

import org.chud.springuniapi.application_events.event.EnrollUserEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EnrollListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEnrolled(EnrollUserEvent event) {
        System.out.println("User " +  event.getUserId() +" enrolled in course "+  event.getCourseId() +" ("+ event.getCourseName() +")");
    }
}
