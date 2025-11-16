package com.pocopi.api.models.form;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "form_question_slider_label",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"form_question_id", "number"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestionSliderLabelModel {
    public static final int LABEL_MIN_LEN = 1;
    public static final int LABEL_MAX_LEN = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "form_question_id", nullable = false)
    private FormQuestionModel formQuestion;

    @Column(name = "number", nullable = false, columnDefinition = "int2 unsigned")
    private int number;

    @Size(min = LABEL_MIN_LEN, max = LABEL_MAX_LEN)
    @NotNull
    @Column(name = "label", nullable = false, length = LABEL_MAX_LEN)
    private String label;
}
