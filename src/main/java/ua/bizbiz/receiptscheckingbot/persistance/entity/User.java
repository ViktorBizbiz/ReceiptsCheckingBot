package ua.bizbiz.receiptscheckingbot.persistance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {

    static final String SEQ_NAME = "user_seq";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1, initialValue = 3)
    @Column(name = "user_id")
    Long userId;

    @Column(name = "chat_id")
    Long chatId;

    @Column(name = "first_name")
    String firstName;

    @Column(name = "last_name")
    String lastName;

    @Column(name = "user_name")
    String userName;

    @Column(name = "phone_number")
    String phoneNumber;

    @Column(name = "registered_at")
    Timestamp registeredAt;

    @Column(name = "sold_packages")
    Integer soldPackages;

    @Column(name = "score")
    Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    Role role;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "address")
    String address;

    @Column(name = "farm_chain")
    String farmChain;

}

