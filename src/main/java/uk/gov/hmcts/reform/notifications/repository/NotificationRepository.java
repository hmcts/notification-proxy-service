package uk.gov.hmcts.reform.notifications.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.notifications.model.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {

    Optional<List<Notification>> findByReference(String paymentReference);
}
