package com.pocopi.api.models.test;

import com.pocopi.api.converters.TestOptionEventTypeJpaConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(
    name = "user_test_option_log",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"attempt_id", "option_id", "type", "timestamp"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTestOptionLogModel {
    public static final int COORD_MIN = 0;
    public static final int COORD_MAX = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int8 unsigned")
    @Setter(AccessLevel.NONE)
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "attempt_id", nullable = false)
    private UserTestAttemptModel attempt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "option_id", nullable = false)
    private TestOptionModel option;

    @NotNull
    @Convert(converter = TestOptionEventTypeJpaConverter.class)
    @Column(name = "type", nullable = false)
    private TestOptionEventType type;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Min(COORD_MIN)
    @Max(COORD_MAX)
    @Column(name = "x")
    private Byte x;

    @Min(COORD_MIN)
    @Max(COORD_MAX)
    @Column(name = "y")
    private Byte y;
}
