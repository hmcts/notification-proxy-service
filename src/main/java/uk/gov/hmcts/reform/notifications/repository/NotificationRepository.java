package uk.gov.hmcts.reform.notifications.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.notifications.model.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {
}
