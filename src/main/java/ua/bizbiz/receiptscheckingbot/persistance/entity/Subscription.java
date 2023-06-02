package ua.bizbiz.receiptscheckingbot.persistance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    User user;

    @ManyToOne()
    @JoinColumn(name = "promotion_id", referencedColumnName = "id")
    Promotion promotion;

    @Column(name = "current_quantity")
    Integer currentQuantity;

    @Column(name = "current_bonus")
    Integer currentBonus;
}
