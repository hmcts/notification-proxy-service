package uk.gov.hmcts.reform.notifications.repository;


import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@SuppressWarnings("PMD")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

}
