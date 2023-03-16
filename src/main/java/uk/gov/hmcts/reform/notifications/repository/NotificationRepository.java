package uk.gov.hmcts.reform.notifications.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.notifications.model.Notification;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Integer> {

   Optional<List<Notification>> findByReferenceOrderByDateUpdatedDesc(String reference);

    long deleteByReference(String reference);

   @Query("select n from Notification n "
        + "where n.reference = ?1  AND n.notificationType = ?2 order by n.dateUpdated desc")
   Optional<List<Notification>> findByReferenceAndNotificationTypeOrderByDateUpdatedDesc(String reference,
                                                                                         String notificationType);
}
