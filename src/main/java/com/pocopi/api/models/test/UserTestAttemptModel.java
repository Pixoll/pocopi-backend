package com.pocopi.api.models.test;

import com.pocopi.api.models.user.UserModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
    name = "user_test_attempt",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "group_id", "start"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTestAttemptModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int8 unsigned")
    @Setter(AccessLevel.NONE)
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "group_id", nullable = false)
    private TestGroupModel group;

    @NotNull
    @Column(name = "start", nullable = false)
    private Instant start;

    @Column(name = "end")
    private Instant end;
}
