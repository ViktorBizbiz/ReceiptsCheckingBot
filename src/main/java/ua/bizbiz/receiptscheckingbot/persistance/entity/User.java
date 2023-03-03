package ua.bizbiz.receiptscheckingbot.persistance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    Chat chat;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "registered_at")
    Timestamp registeredAt;

    @Column(name = "sold_packages")
    Integer soldPackages;

    @Column(name = "score")
    Integer score;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "role")
    Role role;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "address")
    String address;

    @Column(name = "pharmacy_chain")
    String pharmacyChain;

    @Column(name = "city_of_pharmacy")
    String cityOfPharmacy;

    @Column(name = "secret_code")
    Long secretCode;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    List<Subscription> subscriptions;
}

