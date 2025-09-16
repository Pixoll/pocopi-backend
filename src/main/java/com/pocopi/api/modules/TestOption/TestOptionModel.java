package com.pocopi.api.modules.TestOption;

import com.pocopi.api.modules.Image.ImageModel;
import com.pocopi.api.modules.TestQuestion.TestQuestionModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "test_option", uniqueConstraints = {@UniqueConstraint(columnNames = {"question_id", "order"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOptionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    @Setter(AccessLevel.NONE)
    private int id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestionModel question;

    @Column(name = "order", columnDefinition = "tinyint UNSIGNED not null")
    private byte order;

    @Size(max = 100)
    @Column(name = "text", length = 100)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private ImageModel image;

    @NotNull
    @Column(name = "correct", nullable = false)
    private boolean correct;

}
