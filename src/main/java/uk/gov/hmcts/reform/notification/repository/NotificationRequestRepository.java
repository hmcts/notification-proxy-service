package uk.gov.hmcts.reform.notification.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.notification.dtos.request.NotificationRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRequestRepository extends CrudRepository<NotificationRequest, Integer> {

    Optional<List<NotificationRequest>> findByReference(String reference);


    @Modifying
    @Transactional
    @Query("update NotificationRequest n set n.notificationSent = true where n.id = :id")
    void updateNotificationSent(@Param("id") Integer id);
}
