package uk.gov.hmcts.reform.notifications.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder(builderMethodName = "serviceContactWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "service_contact")
public class ServiceContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "service_name", nullable = false)
    String serviceName;

    @Column(name = "service_mailbox")
    String serviceMailbox;

}
