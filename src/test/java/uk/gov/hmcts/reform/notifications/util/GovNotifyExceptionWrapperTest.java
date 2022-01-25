package uk.gov.hmcts.reform.notifications.util;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import uk.gov.hmcts.reform.notifications.exceptions.GovNotifyException;

import uk.gov.service.notify.NotificationClientException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;


@ActiveProfiles({"local", "test"})
@SpringBootTest(webEnvironment = MOCK)
@SuppressWarnings("PMD")
public class GovNotifyExceptionWrapperTest {

     GovNotifyExceptionWrapper govNotifyExceptionWrapper = new GovNotifyExceptionWrapper();

    @Mock
    private NotificationClientException notificationClientException;

    @Mock
    GovNotifyErrorMessage GovNotifyErrorMessage;

    @Test
    public void testMapGovNotifyEmailExceptionfor403Error() {

        when(notificationClientException.getHttpResult()).thenReturn(403);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyEmailException(notificationClientException);
        assertEquals("Internal Server Error, invalid API key", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyEmailExceptionfor429Error() {

        when(notificationClientException.getHttpResult()).thenReturn(429);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyEmailException(notificationClientException);
        assertEquals("Internal Server Error, send limit exceeded", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyEmailExceptionfor500Error() {

        when(notificationClientException.getHttpResult()).thenReturn(500);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyEmailException(notificationClientException);
        assertEquals("Service is not available, please try again", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyEmailExceptionfor502Error() {

        when(notificationClientException.getHttpResult()).thenReturn(502);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyEmailException(notificationClientException);
        assertEquals("Internal Server Error", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyLetterExceptionfor403Error() {

        when(notificationClientException.getHttpResult()).thenReturn(403);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyLetterException(notificationClientException);
        assertEquals("Internal Server Error, invalid API key", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyLetterExceptionfor429Error() {

        when(notificationClientException.getHttpResult()).thenReturn(429);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyLetterException(notificationClientException);
        assertEquals("Internal Server Error, send limit exceeded", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyLetterExceptionfor500Error() {

        when(notificationClientException.getHttpResult()).thenReturn(500);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyLetterException(notificationClientException);
        assertEquals("Service is not available, please try again", govNotifyException.getMessage());

    }

    @Test
    public void testMapGovNotifyLetterExceptionfor502Error() {

        when(notificationClientException.getHttpResult()).thenReturn(502);
        GovNotifyException govNotifyException= govNotifyExceptionWrapper.mapGovNotifyLetterException(notificationClientException);
        assertEquals("Internal Server Error", govNotifyException.getMessage());

    }
}
