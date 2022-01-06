package uk.gov.hmcts.reform.notifications.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

import javax.persistence.*;


@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    private String reference;

    @ToString.Exclude
    @Column(name = "notification_type")
    private String notificationType;

    @ToString.Exclude
    @Column(name = "template_id")
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
//    @OneToMany(cascade = CascadeType.ALL)
//    @JoinColumn(name = "notification_id", referencedColumnName = "id", nullable = false)
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "notification", cascade = CascadeType.ALL)
    private ContactDetails contactDetails;


}
