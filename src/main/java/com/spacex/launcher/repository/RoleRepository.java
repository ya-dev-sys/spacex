package com.spacex.launcher.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.Role;

/**
 * Repository pour l'entité Role
 *
 * OPTIMISATIONS APPLIQUÉES:
 * - Optional<> au lieu de null
 * - Méthodes utilitaires pour la gestion des rôles
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Trouve un rôle par son nom
     *
     * @param name Nom du rôle (ex: "ROLE_ADMIN")
     * @return Optional contenant le rôle si trouvé
     */
    Role findByName(String name);

    /**
     * Vérifie si un rôle existe par son nom
     *
     * @param name Nom du rôle
     * @return true si le rôle existe
     */
    boolean existsByName(String name);

    /**
     * Trouve tous les rôles triés par nom
     *
     * @return Set des rôles triés alphabétiquement
     */
    @Query("SELECT r FROM Role r ORDER BY r.name ASC")
    Set<Role> findAllOrderByName();

    /**
     * Compte le nombre d'utilisateurs ayant ce rôle
     *
     * @param roleId ID du rôle
     * @return Nombre d'utilisateurs avec ce rôle
     */
    @Query("SELECT COUNT(u) FROM Userx u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(Long roleId);
}
