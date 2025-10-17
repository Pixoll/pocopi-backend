package com.pocopi.api.repositories;

import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    @Query(value = "select * from user u where u.id = :userId", nativeQuery = true)
    UserModel getUserByUserId(@Param("userId") int userId);

    @Query(value = "select u.id from user u", nativeQuery = true)
    List<Integer> getAllUserIds();

    boolean existsByUsername(String username);

    @Query(value = "select * from user u where u.username = :username", nativeQuery = true)
    UserModel findByUsername(@Param("username") String username);

    boolean existsByEmail(String email);

    @Query(value = "select * from user", nativeQuery = true)
    List<UserModel> getAllUsers();

    @Modifying
    @Query(
        value = "insert into user (username, anonymous, name, email, age, password)"
                + "values (:username, :anonymous, :name, :email, :age, :password)",
        nativeQuery = true
    )
    void insertNewUser(
        @Param("username") String username,
        @Param("anonymous") boolean anonymous,
        @Param("name") String name,
        @Param("email") String email,
        @Param("age") Byte age,
        @Param("password") String password
    );
}
