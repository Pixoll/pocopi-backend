package com.pocopi.api.modules.FormQuestionOption;

import com.pocopi.api.modules.FormQuestion.FormQuestionModel;
import com.pocopi.api.modules.Image.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form_question_option", uniqueConstraints = {@UniqueConstraint(columnNames = {"order", "form_question_id"})})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FormQuestionOptionModel {
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

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 100)
    @Column(name = "text", length = 100)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image;

}
