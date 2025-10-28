package com.spacex.launcher.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.LaunchPad;

/**
 * Repository pour l'entité LaunchPad
 *
 * Fournit des méthodes de recherche avancées pour les sites de lancement
 */
@Repository
public interface LaunchPadRepository extends JpaRepository<LaunchPad, String> {

    /**
     * Trouve un launchpad par son nom (insensible à la casse)
     *
     * @param name Nom du launchpad
     * @return Optional contenant le launchpad si trouvé
     */
    Optional<LaunchPad> findByNameIgnoreCase(String name);

    /**
     * Trouve tous les launchpads d'une région
     *
     * @param region Région (ex: "California")
     * @return Liste des launchpads de cette région
     */
    List<LaunchPad> findByRegion(String region);

    /**
     * Trouve tous les launchpads d'une localité
     *
     * @param locality Localité (ex: "Cape Canaveral")
     * @return Liste des launchpads de cette localité
     */
    List<LaunchPad> findByLocality(String locality);

    /**
     * Trouve les launchpads dans un rayon géographique
     * Utilise la formule de Haversine pour calculer la distance
     *
     * @param latitude  Latitude du point de référence
     * @param longitude Longitude du point de référence
     * @param radiusKm  Rayon de recherche en kilomètres
     * @return Liste des launchpads dans le rayon
     */
    @Query("""
            SELECT lp FROM LaunchPad lp
            WHERE (6371 * acos(
                cos(radians(:latitude)) * cos(radians(lp.latitude)) *
                cos(radians(lp.longitude) - radians(:longitude)) +
                sin(radians(:latitude)) * sin(radians(lp.latitude))
            )) <= :radiusKm
            """)
    List<LaunchPad> findWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm);

    /**
     * Recherche de launchpads par nom partiel (LIKE)
     *
     * @param name     Fragment du nom
     * @param pageable Configuration de pagination
     * @return Page de launchpads correspondants
     */
    @Query("SELECT lp FROM LaunchPad lp WHERE LOWER(lp.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<LaunchPad> searchByName(String name, Pageable pageable);

    /**
     * Vérifie si un launchpad existe par son nom
     *
     * @param name Nom du launchpad
     * @return true si un launchpad avec ce nom existe
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Trouve tous les launchpads triés par nom
     *
     * @return Liste des launchpads triés alphabétiquement
     */
    List<LaunchPad> findAllByOrderByNameAsc();

    /**
     * Compte le nombre de launchpads par région
     *
     * @param region Région
     * @return Nombre de launchpads dans cette région
     */
    long countByRegion(String region);

    /**
     * Trouve tous les launchpads avec coordonnées valides
     * Utile pour afficher sur une carte
     *
     * @return Liste des launchpads avec latitude et longitude non nulles
     */
    @Query("SELECT lp FROM LaunchPad lp WHERE lp.latitude IS NOT NULL AND lp.longitude IS NOT NULL")
    List<LaunchPad> findAllWithCoordinates();
}
