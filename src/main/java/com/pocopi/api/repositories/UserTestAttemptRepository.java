package com.pocopi.api.repositories;

import com.pocopi.api.models.test.UserTestAttemptModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import java.util.Optional;

public interface UserTestAttemptRepository extends JpaRepository<UserTestAttemptModel, Long> {
    @NativeQuery(
        """
            select ta.*
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                where g.config_version = :configVersion
                  and ta.user_id = :userId
                  and ta.end is null
            """
    )
    Optional<UserTestAttemptModel> findUnfinishedAttempt(int configVersion, int userId);

    @NativeQuery(
        """
            select ta.*
                from user_test_attempt    ta
                    inner join test_group g on g.id = ta.group_id
                where g.config_version = :configVersion
                  and ta.user_id = :userId
                  and ta.end is not null
                order by ta.start
                limit 1
            """
    )
    Optional<UserTestAttemptModel> findLatestFinishedAttempt(int configVersion, int userId);

    default boolean hasUnfinishedAttempt(int configVersion, int userId) {
        return findUnfinishedAttempt(configVersion, userId).isPresent();
    }
}
