package com.pocopi.api.models.test;

import com.pocopi.api.models.config.ImageModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_option", uniqueConstraints = {@UniqueConstraint(columnNames = {"question_id", "`order`"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOptionModel {
    public static final int TEXT_MIN_LEN = 1;
    public static final int TEXT_MAX_LEN = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "int4 unsigned")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestionModel question;

    @Column(name = "`order`", nullable = false, columnDefinition = "int1 unsigned")
    private short order;

    @Builder.Default
    @Size(min = TEXT_MIN_LEN, max = TEXT_MAX_LEN)
    @Column(name = "text", length = TEXT_MAX_LEN)
    private String text = null;

    @Builder.Default
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image = null;

    @Column(name = "correct", nullable = false)
    private boolean correct;
}
