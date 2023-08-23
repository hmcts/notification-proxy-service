package uk.gov.hmcts.reform.notification.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.hmcts.reform.notification.dtos.response.MailAddress;

@Entity
@Getter
@Setter
@ToString
@Builder(builderMethodName = "serviceContactWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_contact")
@TypeDef(name = "json", typeClass = JsonType.class)
public class ServiceContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "service_name", nullable = false)
    String serviceName;

    @Column(name = "service_mailbox")
    String serviceMailbox;

    @Column(name = "from_email_address")
    private String fromEmailAddress;

    @Type(type = "json")
    @Column(columnDefinition = "json", name = "from_mail_address")
    private MailAddress fromMailAddress;
}
