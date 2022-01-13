package uk.gov.hmcts.reform.notifications.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "test"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GovNotifyExceptionWrapperTest {


    GovNotifyExceptionWrapper exceptionWrapper = new GovNotifyExceptionWrapper();

    @Test
    public void emailExceptionWrapperTest() throws Exception {
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyEmailException(403, "test").getMessage(), "Internal Server Error, invalid API key");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyEmailException(429, "test").getMessage(), "Internal Server Error, send limit exceeded");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyEmailException(500, "test").getMessage(), "Service is not available, please try again");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyEmailException(504, "test").getMessage(), "Internal Server Error");
    }

    @Test
    public void letterExceptionWrapperTest() throws Exception {
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyLetterException(403, "test").getMessage(), "Internal Server Error, invalid API key");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyLetterException(429, "test").getMessage(), "Internal Server Error, send limit exceeded");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyLetterException(500, "test").getMessage(), "Service is not available, please try again");
        Assertions.assertEquals(exceptionWrapper.mapGovNotifyLetterException(504, "test").getMessage(), "Internal Server Error");
    }
}
