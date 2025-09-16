package com.pocopi.api.modules.TestProtocol;

import com.pocopi.api.modules.Config.ConfigModel;
import com.pocopi.api.modules.TestGroup.TestGroupModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_protocol", uniqueConstraints = {@UniqueConstraint(columnNames = {"config_version", "group_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestProtocolModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "config_version", nullable = false)
    private ConfigModel configVersion;

    @Size(max = 25)
    @NotNull
    @Column(name = "label", nullable = false, length = 25)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "group_id")
    private TestGroupModel group;

    @Column(name = "allow_previous_phase")
    private boolean allowPreviousPhase = true;

    @Column(name = "allow_previous_question")
    private boolean allowPreviousQuestion = true;

    @Column(name = "allow_skip_question")
    private boolean allowSkipQuestion = true;

    @Column(name = "randomize_phases")
    private boolean randomizePhases = false;

}
