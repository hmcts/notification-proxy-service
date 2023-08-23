package uk.gov.hmcts.reform.notification.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "notification_refund_reasons")
@Builder(builderMethodName = "notificationRefundReasonWith")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class NotificationRefundReasons {

    @Id
    @Column(name = "refund_reason_code", nullable = false)
    String refundReasonCode;

    @Column(name = "refund_reason", nullable = false)
    String refundReason;

    @Column(name = "refund_reason_notification", nullable = false)
    String refundReasonNotification;

}
