package com.spacex.launcher.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.Userx;

/**
 * Repository pour l'entité Userx
 *
 * OPTIMISATIONS APPLIQUÉES:
 * - JOIN FETCH pour charger les rôles (évite N+1)
 * - Optional<> au lieu de null pour meilleure gestion
 * - Queries explicites pour clarté
 */
@Repository
public interface UserRepository extends JpaRepository<Userx, UUID> {

    /**
     * Trouve un utilisateur par username avec ses rôles
     * JOIN FETCH évite le lazy loading problem
     *
     * @param username Username de l'utilisateur
     * @return Optional contenant l'utilisateur avec ses rôles
     */
    @Query("SELECT u FROM Userx u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<Userx> findByUsernameWithRoles(@Param("username") String username);

    /**
     * Trouve un utilisateur par email avec ses rôles
     * Utilisé pour l'authentification
     *
     * @param email Email de l'utilisateur
     * @return Optional contenant l'utilisateur avec ses rôles
     */
    @Query("SELECT u FROM Userx u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<Userx> findByEmailWithRoles(@Param("email") String email);

    /**
     * Trouve un utilisateur par username (sans les rôles)
     * Utilisé pour les vérifications d'unicité
     *
     * @param username Username de l'utilisateur
     * @return Optional contenant l'utilisateur
     */
    Optional<Userx> findByUsername(String username);

    /**
     * Trouve un utilisateur par email (sans les rôles)
     * Utilisé pour les vérifications d'unicité
     *
     * @param email Email de l'utilisateur
     * @return Optional contenant l'utilisateur
     */
    Optional<Userx> findByEmail(String email);

    /**
     * Vérifie si un username existe déjà
     *
     * @param username Username à vérifier
     * @return true si le username existe
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe déjà
     *
     * @param email Email à vérifier
     * @return true si l'email existe
     */
    boolean existsByEmail(String email);

    /**
     * Compte le nombre d'utilisateurs ayant un rôle spécifique
     *
     * @param roleName Nom du rôle (ex: "ROLE_ADMIN")
     * @return Nombre d'utilisateurs avec ce rôle
     */
    @Query("SELECT COUNT(DISTINCT u) FROM Userx u JOIN u.roles r WHERE r.name = :roleName")
    long countByRole(@Param("roleName") String roleName);

    /**
     * Trouve un utilisateur par username OU email
     * Utilisé pour l'authentification flexible
     *
     * @param identifier Username ou email
     * @return Optional contenant l'utilisateur avec ses rôles
     */
    @Query("""
            SELECT u FROM Userx u
            LEFT JOIN FETCH u.roles
            WHERE u.username = :identifier OR u.email = :identifier
            """)
    Optional<Userx> findByUsernameOrEmailWithRoles(@Param("identifier") String identifier);
}
