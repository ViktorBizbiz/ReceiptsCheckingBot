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
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    Long id;

    @Column(name = "name")
    String name;

    @Column(name = "min_quantity")
    Integer minQuantity;

    @Column(name = "completion_bonus")
    Integer completionBonus;

    @Column(name = "resale_bonus")
    Integer resaleBonus;

    @OneToOne(mappedBy = "promotion")
    Subscription subscription;
}
