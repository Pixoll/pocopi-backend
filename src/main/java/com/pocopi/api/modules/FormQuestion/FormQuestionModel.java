package com.pocopi.api.modules.FormQuestion;

import com.pocopi.api.modules.Image.ImageModel;
import com.pocopi.api.modules.forms.FormModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "form_question", uniqueConstraints = {@UniqueConstraint(columnNames = {"order", "form_id"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "form_id", nullable = false)
    private FormModel form;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 50)
    @NotBlank
    @NotNull
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Size(max = 100)
    @NotBlank
    @Column(name = "text", length = 100)
    private String text = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "required", nullable = false)
    private boolean required = true;

    @NotNull
    @Lob
    @Column(name = "type", nullable = false)
    private TextType type;

    @Column(name = "min", columnDefinition = "smallint UNSIGNED")
    private short min;

    @Column(name = "max", columnDefinition = "smallint UNSIGNED")
    private short max;

    @Column(name = "step", columnDefinition = "smallint UNSIGNED")
    private short step;

    @Column(name = "other")
    private boolean other;

    @Column(name = "min_length", columnDefinition = "smallint UNSIGNED")
    private short minLength;

    @Column(name = "max_length", columnDefinition = "smallint UNSIGNED")
    private short maxLength;

    @Size(max = 50)
    @Column(name = "placeholder", length = 50)
    private String placeholder;
}
