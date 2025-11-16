package com.pocopi.api.models.form;

import com.pocopi.api.converters.FormQuestionTypeConverter;
import com.pocopi.api.models.config.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form_question", uniqueConstraints = {@UniqueConstraint(columnNames = {"form_id", "`order`"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "form_id", nullable = false)
    private FormModel form;

    @Column(name = "`order`", nullable = false, columnDefinition = "int1 unsigned")
    private byte order;

    @Size(min = 1, max = 50)
    @NotNull
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Builder.Default
    @Size(min = 1, max = 200)
    @Column(name = "text", length = 200)
    private String text = null;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;

    @Builder.Default
    @NotNull
    @Column(name = "required", nullable = false)
    private boolean required = true;

    @Builder.Default
    @Column(name = "min", columnDefinition = "int2 unsigned")
    private Integer min = null;

    @Builder.Default
    @Column(name = "max", columnDefinition = "int2 unsigned")
    private Integer max = null;

    @Builder.Default
    @Column(name = "step", columnDefinition = "int2 unsigned")
    private Integer step = null;

    @Builder.Default
    @Column(name = "other")
    private Boolean other = null;

    @Builder.Default
    @Column(name = "min_length", columnDefinition = "int2 unsigned")
    private Integer minLength = null;

    @Builder.Default
    @Column(name = "max_length", columnDefinition = "int2 unsigned")
    private Integer maxLength = null;

    @Builder.Default
    @Size(min = 1, max = 50)
    @Column(name = "placeholder", length = 50)
    private String placeholder = null;

    @NotNull
    @Convert(converter = FormQuestionTypeConverter.class)
    @Column(name = "type", nullable = false)
    private FormQuestionType type;
}
