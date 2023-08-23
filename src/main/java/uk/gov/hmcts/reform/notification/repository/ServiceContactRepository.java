package uk.gov.hmcts.reform.notification.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.notification.model.ServiceContact;

@Repository
public interface ServiceContactRepository extends CrudRepository<ServiceContact, Integer> {

    Optional<ServiceContact> findByServiceName(String serviceName);
}
