package com.spacex.launcher.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.Launch;

/**
 * Repository pour l'entité Launch
 *
 * OPTIMISATIONS APPLIQUÉES:
 * - Utilisation de @Query avec JOIN FETCH pour éviter N+1 problem
 * - Queries dédiées pour les cas d'usage métier
 * - Méthodes nommées selon Spring Data conventions
 */
@Repository
public interface LaunchRepository extends JpaRepository<Launch, String> {

    /**
     * Trouve le prochain lancement (date future la plus proche)
     * Utilisé pour le KPI "Next Launch"
     *
     * @param now Date actuelle
     * @return Optional contenant le prochain lancement
     */
    @Query("""
            SELECT l FROM Launch l
            LEFT JOIN FETCH l.rocket
            LEFT JOIN FETCH l.launchPad
            WHERE l.dateUtc > :now
            ORDER BY l.dateUtc ASC
            LIMIT 1
            """)
    Optional<Launch> findNextLaunch(@Param("now") Instant now);

    /**
     * Récupère tous les lancements avec pagination
     * JOIN FETCH pour éviter le N+1 problem
     *
     * @param pageable Configuration de pagination
     * @return Page de lancements avec rocket et launchpad chargés
     */
    @Query(value = """
            SELECT DISTINCT l FROM Launch l
            LEFT JOIN FETCH l.rocket
            LEFT JOIN FETCH l.launchPad
            ORDER BY l.dateUtc DESC
            """, countQuery = "SELECT COUNT(l) FROM Launch l")
    Page<Launch> findAllWithDetails(Pageable pageable);

    /**
     * Filtre par année avec JOIN FETCH
     *
     * @param start    Début de l'année
     * @param end      Fin de l'année
     * @param pageable Configuration de pagination
     * @return Page de lancements pour l'année donnée
     */
    @Query(value = """
            SELECT DISTINCT l FROM Launch l
            LEFT JOIN FETCH l.rocket
            LEFT JOIN FETCH l.launchPad
            WHERE l.dateUtc BETWEEN :start AND :end
            ORDER BY l.dateUtc DESC
            """, countQuery = """
            SELECT COUNT(l) FROM Launch l
            WHERE l.dateUtc BETWEEN :start AND :end
            """)
    Page<Launch> findByYearWithDetails(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    /**
     * Filtre par statut de succès avec JOIN FETCH
     *
     * @param success  Statut de succès (true/false)
     * @param pageable Configuration de pagination
     * @return Page de lancements filtrés par succès
     */
    @Query(value = """
            SELECT DISTINCT l FROM Launch l
            LEFT JOIN FETCH l.rocket
            LEFT JOIN FETCH l.launchPad
            WHERE l.success = :success
            ORDER BY l.dateUtc DESC
            """, countQuery = """
            SELECT COUNT(l) FROM Launch l
            WHERE l.success = :success
            """)
    Page<Launch> findBySuccessWithDetails(
            @Param("success") Boolean success,
            Pageable pageable);

    /**
     * Récupère un lancement par ID avec tous ses détails
     * Utilisé pour la page de détail
     *
     * @param id ID du lancement
     * @return Optional contenant le lancement avec toutes ses relations
     */
    @Query("""
            SELECT DISTINCT l FROM Launch l
            LEFT JOIN FETCH l.rocket
            LEFT JOIN FETCH l.launchPad
            LEFT JOIN FETCH l.payloads
            WHERE l.id = :id
            """)
    Optional<Launch> findByIdWithDetails(@Param("id") String id);

    /**
     * Compte le nombre total de lancements réussis
     * Utilisé pour les KPIs
     *
     * @return Nombre de lancements avec success=true
     */
    @Query("SELECT COUNT(l) FROM Launch l WHERE l.success = true")
    long countSuccessfulLaunches();

    /**
     * Compte le nombre total de lancements échoués
     *
     * @return Nombre de lancements avec success=false
     */
    @Query("SELECT COUNT(l) FROM Launch l WHERE l.success = false")
    long countFailedLaunches();

    /**
     * Vérifie si un lancement existe déjà (pour éviter les doublons lors de la
     * sync)
     *
     * @param id ID du lancement
     * @return true si le lancement existe
     */
    boolean existsById(String id);

    /**
     * Compte les lancements par année
     * Utilisé pour les statistiques yearly
     *
     * @param start Début de l'année
     * @param end   Fin de l'année
     * @return Nombre de lancements dans l'année
     */
    @Query("""
            SELECT COUNT(l) FROM Launch l
            WHERE l.dateUtc BETWEEN :start AND :end
            """)
    long countByYear(
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Compte les lancements réussis par année
     *
     * @param start Début de l'année
     * @param end   Fin de l'année
     * @return Nombre de lancements réussis dans l'année
     */
    @Query("""
            SELECT COUNT(l) FROM Launch l
            WHERE l.dateUtc BETWEEN :start AND :end
            AND l.success = true
            """)
    long countSuccessfulByYear(
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Trouve tous les lancements d'une fusée spécifique
     *
     * @param rocketId ID de la fusée
     * @param pageable Configuration de pagination
     * @return Page de lancements pour cette fusée
     */
    Page<Launch> findByRocketId(String rocketId, Pageable pageable);

    /**
     * Trouve tous les lancements d'un launchpad spécifique
     *
     * @param launchPadId ID du launchpad
     * @param pageable    Configuration de pagination
     * @return Page de lancements pour ce launchpad
     */
    Page<Launch> findByLaunchPadId(String launchPadId, Pageable pageable);
}
