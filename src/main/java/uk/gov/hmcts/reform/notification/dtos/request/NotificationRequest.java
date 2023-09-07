package uk.gov.hmcts.reform.notification.dtos.request;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "notification_request")
@TypeDef(name = "json", typeClass = JsonType.class)
@ToString
public class NotificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Reference has to be present")
    @ToString.Exclude
    private String reference;

    @ToString.Exclude
    @Column(name = "destination_address")
    private String destinationAddress;

    @ToString.Exclude
    @Column(name = "template_id")
    private String templateId;

    @Type(type = "json")
    @Column(columnDefinition = "json", name = "template_vars")
    private Map<String, Object> templateVars;

    @Column(name = "has_file_attachments")
    private boolean hasFileAttachments;

    @Type(type = "json")
    @Column(columnDefinition = "json", name = "uploaded_documents")
    private Map<String, String> uploadedDocuments;

    @Column(name = "notification_sent")
    private boolean notificationSent;

    @Column(name = "case_id")
    private long caseId;
}
