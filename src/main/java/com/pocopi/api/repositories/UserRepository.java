package com.pocopi.api.repositories;

import com.pocopi.api.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

   @Query(value = "SELECT true FROM user u WHERE u.username = :username", nativeQuery = true)
   boolean existsByUsername(@Param("username") String username);

   @Query(value = "SELECT true FROM user u WHERE u.email = :email", nativeQuery = true)
   boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT * from user", nativeQuery = true)
    List<UserModel> getAllUsers();

    @Modifying
    @Query(
            value = "INSERT into user (username, group_id, anonymous, name, email, age, password)" +
                    "values (:username,:group_id,:anonymous, :name, :email, :age, :password)"
            , nativeQuery = true
    )
    void insertNewUser(@Param("username") String username,
                       @Param("group_id") int group_id,
                       @Param("anonymous") boolean anonymous,
                       @Param("name") String name,
                       @Param("email") String email,
                       @Param("age") byte age,
                       @Param("password") String password
    );
}
