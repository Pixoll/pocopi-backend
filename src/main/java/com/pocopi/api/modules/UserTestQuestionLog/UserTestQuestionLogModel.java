package com.pocopi.api.modules.UserTestQuestionLog;

import com.pocopi.api.modules.TestQuestion.TestQuestionModel;
import com.pocopi.api.modules.User.UserModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_test_question_log", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "question_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTestQuestionLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestionModel question;

    @NotNull
    @Column(name = "start", nullable = false)
    private LocalDateTime start;

    @NotNull
    @Column(name = "end", nullable = false)
    private LocalDateTime end;

}
