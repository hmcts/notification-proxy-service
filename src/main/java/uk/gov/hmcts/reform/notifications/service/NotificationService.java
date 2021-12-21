package uk.gov.hmcts.reform.notifications.service;

import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    void sendEmailNotification() throws NotificationClientException;

    void sendLetterNotification() throws NotificationClientException;
}
