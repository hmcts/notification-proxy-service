package uk.gov.hmcts.reform.notification.model;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Date;

@Entity
@Getter
@Setter
@ToString
@Builder(builderMethodName = "contactDetailsWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contact_details")
public class ContactDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @OneToOne (fetch = FetchType.EAGER)
    @JoinColumn (name="notification_id")
    private Notification notification;


    @ToString.Exclude
    private String addressLine;

    @ToString.Exclude
    private String city;

    @ToString.Exclude
    @Email(message = "Please enter a valid email")
    private String email;

    @ToString.Exclude
    private String county;

    @ToString.Exclude
    private String postcode;

    @ToString.Exclude
    private String country;

    @CreationTimestamp
    @Column(name = "date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private Date dateUpdated;

    @ToString.Exclude
    @Column(name = "created_by")
    private String createdBy;
}
