package com.pocopi.api.modules.TestQuestion;

import com.pocopi.api.modules.Image.ImageModel;
import com.pocopi.api.modules.TestPhase.TestPhaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_question", uniqueConstraints = {@UniqueConstraint(columnNames = {"phase_id", "order"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "phase_id", nullable = false)
    private TestPhaseModel phase;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 100)
    @Column(name = "text", length = 100)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;

    @Column(name = "randomize_options")
    private boolean randomizeOptions = false;

}
