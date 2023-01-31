package ua.bizbiz.receiptscheckingbot.persistance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "chatId")
    Long chatId;

    @Column(name = "firstName")
    String firstName;

    @Column(name = "lastName")
    String lastName;

    @Column(name = "phoneNumber")
    String phoneNumber;

    @Column(name = "soldPackages")
    Integer soldPackages;

    @Column(name = "score")
    Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    Role role;

}

