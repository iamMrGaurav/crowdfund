package com.example.crowdfund.repository;

import com.example.crowdfund.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{ // Long is Primary Key of User Class
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByStripeAccountId(String stripeAccountId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Query("SELECT u.stripeAccountId FROM User u WHERE u.id = :userId")
    String findStripeAccountIdByUserId(@Param("userId") Long userId);
}

/*
Meaning of this = JpaRepository<User (Entity used), Long (Primary Key))>

Why REPOSITORY is INTERFACE ?

// Spring creates this behind the scenes (simplified), so that it can used it <<------
public class SpringGeneratedUserRepository implements UserRepository {
    private EntityManager entityManager;

    @Override
    public Optional<User> findByUsername(String username) {
        // Spring automatically generates this:
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.username = :username", User.class)
            .setParameter("username", username)
            .getResultList()
            .stream()
            .findFirst();
    }

    // Plus all JpaRepository methods...
}
*/
