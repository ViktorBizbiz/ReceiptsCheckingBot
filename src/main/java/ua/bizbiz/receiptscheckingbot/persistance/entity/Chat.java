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
@Table(name = "chats")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "chats_seq")
    @SequenceGenerator(name = "chats_seq", allocationSize = 1)
    Long id;

    Long chatId;

    @Enumerated(value = EnumType.STRING)
    ChatStatus status;

    @OneToOne(mappedBy = "chat")
    User user;

    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", status=" + status +
                ", user=" + user.getId() +
                '}';
    }
}
