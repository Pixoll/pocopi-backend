package com.pocopi.api.modules.FormQuestionSliderLabel;

import com.pocopi.api.modules.FormQuestion.FormQuestionModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form_question_slider_label", uniqueConstraints = {@UniqueConstraint(columnNames = {"number", "form_question_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class FormQuestionSliderLabelModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "form_question_id", nullable = false)
    private FormQuestionModel formQuestion;

    @Column(name = "number", columnDefinition = "tinyint UNSIGNED not null")
    private byte number;

    @Size(max = 50)
    @NotNull
    @Column(name = "label", nullable = false, length = 50)
    private String label;
}
