package uk.gov.hmcts.reform.notification.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.UpdateTimestamp;
import com.vladmihalcea.hibernate.type.json.JsonType;

import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "notification")
@TypeDef(name = "json", typeClass = JsonType.class)
@ToString
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Reference has to be present")
    @ToString.Exclude
    private String reference;

    @ToString.Exclude
    @Column(name = "notification_type")
    private String notificationType;

    @ToString.Exclude
    @Column(name = "template_id")
    @NotNull(message = "Template ID has to be present")
    private String templateId;

    @CreationTimestamp
    @Column(name = "date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private Date dateUpdated;

    @ToString.Exclude
    @Column(name = "created_by")
    private String createdBy;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "notification", cascade = CascadeType.ALL)
    private ContactDetails contactDetails;

    @Type(type = "json")
    @Column(columnDefinition = "json", name = "template_preview")
    private TemplatePreviewDto templatePreview;
}
