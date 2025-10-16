package com.pocopi.api.repositories;

import com.pocopi.api.models.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserModel, Integer> {

    List<UserModel> findAllByGroup_Config_Version(int configVersion);

    @Query(value = "SELECT * FROM user u WHERE u.group_id = :groupId", nativeQuery = true)
    List<UserModel> findAllByGroup_Id(@Param("groupId") int groupId);

    @Query(value = "SELECT * from user u where u.id =:userId",nativeQuery = true)
    UserModel getUserByUserId(@Param("userId") int userId);

    @Query(value = "SELECT u.id from user u ",nativeQuery = true)
    List<Integer> getAllUserIds();

    boolean existsByUsername(String username);

    @Query(value = "SELECT * FROM user u WHERE u.username = :username", nativeQuery = true)
    UserModel findByUsername(@Param("username") String username);

    boolean existsByEmail(String email);

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
