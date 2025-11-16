package com.pocopi.api.models.test;

import com.pocopi.api.models.config.ConfigModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_group", uniqueConstraints = {@UniqueConstraint(columnNames = {"config_version", "label"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestGroupModel {
    public static final int LABEL_MIN_LEN = 1;
    public static final int LABEL_MAX_LEN = 25;
    public static final int PROBABILITY_MIN = 0;
    public static final int PROBABILITY_MAX = 100;
    public static final int GREETING_MIN_LEN = 1;
    public static final int GREETING_MAX_LEN = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel config;

    @Size(min = LABEL_MIN_LEN, max = LABEL_MAX_LEN)
    @NotNull
    @Column(name = "label", nullable = false, length = LABEL_MAX_LEN)
    private String label;

    @Min(PROBABILITY_MIN)
    @Max(PROBABILITY_MAX)
    @Column(name = "probability", nullable = false, columnDefinition = "int1 unsigned")
    private byte probability;

    @Builder.Default
    @Size(min = GREETING_MIN_LEN, max = GREETING_MAX_LEN)
    @Column(name = "greeting", length = GREETING_MAX_LEN)
    private String greeting = null;

    @Builder.Default()
    @Column(name = "allow_previous_phase")
    private boolean allowPreviousPhase = true;

    @Builder.Default()
    @Column(name = "allow_previous_question")
    private boolean allowPreviousQuestion = true;

    @Builder.Default()
    @Column(name = "allow_skip_question")
    private boolean allowSkipQuestion = true;

    @Builder.Default()
    @Column(name = "randomize_phases")
    private boolean randomizePhases = false;
}
