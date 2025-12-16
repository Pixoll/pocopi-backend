package com.pocopi.api.repositories;

import com.pocopi.api.models.user.Role;
import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    boolean existsByUsername(String username);

    Optional<UserModel> findByUsername(String username);

    boolean existsByEmail(String email);

    @NativeQuery(
        """
            select u.*
                from user                        u
                    inner join user_test_attempt ta on ta.user_id = u.id
                where ta.id = :attemptId and ta.end is not null
            """
    )
    Optional<UserModel> findByFinishedAttemptId(long attemptId);

    @NativeQuery(
        """
            select u.*
                from user                        u
                    inner join user_test_attempt ta on ta.user_id = u.id
                where ta.id in :attemptIds
                group by u.id
            """
    )
    List<UserModel> findAllByAttemptIds(List<Long> attemptIds);

    List<UserModel> findAllByRole(Role role);
}
