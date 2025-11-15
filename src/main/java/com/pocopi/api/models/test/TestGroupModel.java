package com.pocopi.api.models.test;

import com.pocopi.api.models.config.ConfigModel;
import jakarta.persistence.*;
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

    @Size(min = 1, max = 25)
    @NotNull
    @Column(name = "label", nullable = false, length = 25)
    private String label;

    @Column(name = "probability", nullable = false, columnDefinition = "int1 unsigned")
    private byte probability;

    @Builder.Default
    @Size(min = 1, max = 2000)
    @Column(name = "greeting", length = 2000)
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
