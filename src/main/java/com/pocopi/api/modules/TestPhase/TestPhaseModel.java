package com.pocopi.api.modules.TestPhase;

import com.pocopi.api.modules.TestProtocol.TestProtocolModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_phase", uniqueConstraints = {@UniqueConstraint(columnNames = {"order", "protocol_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPhaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "protocol_id", nullable = false)
    private TestProtocolModel protocol;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Column(name = "randomize_questions")
    private boolean randomizeQuestions = false;
}
