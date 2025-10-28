package com.pocopi.api.models.form;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_form_answer", indexes = {@Index(columnList = "user_id, question_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFormAnswerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "form_sub_id", nullable = false)
    private UserFormSubmissionModel formSubmission;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "question_id", nullable = false)
    private FormQuestionModel question;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "option_id")
    private FormQuestionOptionModel option = null;

    @Column(name = "value", columnDefinition = "int2 unsigned")
    private Short value = null;

    @Size(min = 1, max = 1000)
    @Column(name = "answer", length = 1000)
    private String answer = null;
}
