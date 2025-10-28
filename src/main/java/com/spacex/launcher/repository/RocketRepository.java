package com.spacex.launcher.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.Rocket;

/**
 * Repository pour l'entité Rocket
 *
 * Fournit des méthodes de recherche avancées pour les fusées
 */
@Repository
public interface RocketRepository extends JpaRepository<Rocket, String> {

    /**
     * Trouve une fusée par son nom (insensible à la casse)
     *
     * @param name Nom de la fusée (ex: "Falcon 9")
     * @return Optional contenant la fusée si trouvée
     */
    Optional<Rocket> findByNameIgnoreCase(String name);

    /**
     * Trouve toutes les fusées actives
     *
     * @return Liste des fusées actives
     */
    List<Rocket> findByActiveTrue();

    /**
     * Trouve toutes les fusées inactives
     *
     * @return Liste des fusées inactives
     */
    List<Rocket> findByActiveFalse();

    /**
     * Trouve les fusées par pays
     *
     * @param country Pays (ex: "United States")
     * @return Liste des fusées de ce pays
     */
    List<Rocket> findByCountry(String country);

    /**
     * Trouve les fusées par compagnie
     *
     * @param company Nom de la compagnie (ex: "SpaceX")
     * @return Liste des fusées de cette compagnie
     */
    List<Rocket> findByCompany(String company);

    /**
     * Compte le nombre de fusées actives
     *
     * @return Nombre de fusées actives
     */
    long countByActiveTrue();

    /**
     * Recherche de fusées par nom partiel (LIKE)
     *
     * @param name     Fragment du nom
     * @param pageable Configuration de pagination
     * @return Page de fusées correspondantes
     */
    @Query("SELECT r FROM Rocket r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Rocket> searchByName(String name, Pageable pageable);

    /**
     * Vérifie si une fusée existe par son nom
     * Utile pour éviter les doublons lors de la synchronisation
     *
     * @param name Nom de la fusée
     * @return true si une fusée avec ce nom existe
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Trouve toutes les fusées triées par nom
     *
     * @return Liste des fusées triées alphabétiquement
     */
    List<Rocket> findAllByOrderByNameAsc();
}
