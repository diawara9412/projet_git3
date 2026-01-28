package com.repair.machinemanagement.repository;

import com.repair.machinemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByIdentifiant(String identifiant);
    
    @Query("SELECT u FROM User u WHERE u.email = :login OR u.identifiant = :login")
    Optional<User> findByEmailOrIdentifiant(@Param("login") String email, @Param("login") String identifiant);
    
    List<User> findByRole(User.Role role);
    Boolean existsByEmail(String email);
    Boolean existsByNumero(String numero);
    Boolean existsByRole(User.Role role);
    Boolean existsByIdentifiant(String identifiant);
}
