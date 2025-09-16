package com.pocopi.api.modules.UserTestOptionLog;

import com.pocopi.api.modules.TestOption.TestOptionModel;
import com.pocopi.api.modules.User.UserModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_test_option_log", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "option_id", "type", "timestamp"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTestOptionLogModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Setter(AccessLevel.NONE)
    private long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false)
    private TestOptionModel option;

    @NotNull
    @Lob
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

}
