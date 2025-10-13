package com.pocopi.api.models.test;

import com.pocopi.api.models.image.ImageModel;
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
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "phase_id", nullable = false)
    private TestPhaseModel phase;

    @Column(name = "order", nullable = false, columnDefinition = "int1 unsigned")
    private byte order;

    @Size(min = 1, max = 100)
    @Column(name = "text", length = 100)
    private String text = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;

    @Column(name = "randomize_options")
    private boolean randomizeOptions = false;
}
