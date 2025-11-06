package com.pocopi.api.models.form;

import com.pocopi.api.models.config.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "form_question_option",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"form_question_id", "`order`"})}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestionOptionModel {
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

    @Column(name = "`order`", nullable = false, columnDefinition = "int1 unsigned")
    private byte order;

    @Builder.Default
    @Size(min = 1, max = 100)
    @Column(name = "text", length = 100)
    private String text = null;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;
}
