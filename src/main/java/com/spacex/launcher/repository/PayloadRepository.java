package com.spacex.launcher.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spacex.launcher.model.Payload;

/**
 * Repository pour l'entité Payload
 *
 * Fournit des méthodes pour analyser les charges utiles des lancements
 */
@Repository
public interface PayloadRepository extends JpaRepository<Payload, String> {

    /**
     * Trouve tous les payloads d'un type spécifique
     *
     * @param type Type de payload (ex: "Satellite", "Dragon")
     * @return Liste des payloads de ce type
     */
    List<Payload> findByType(String type);

    /**
     * Trouve tous les payloads d'une orbite spécifique
     *
     * @param orbit Orbite (ex: "LEO", "GTO", "ISS")
     * @return Liste des payloads sur cette orbite
     */
    List<Payload> findByOrbit(String orbit);

    /**
     * Trouve tous les payloads d'un client
     *
     * @param customer Nom du client
     * @return Liste des payloads de ce client
     */
    List<Payload> findByCustomer(String customer);

    /**
     * Recherche des payloads par nom partiel
     *
     * @param name     Fragment du nom
     * @param pageable Configuration de pagination
     * @return Page de payloads correspondants
     */
    @Query("SELECT p FROM Payload p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Payload> searchByName(@Param("name") String name, Pageable pageable);

    /**
     * Trouve les payloads dans une plage de masse
     *
     * @param minMassKg Masse minimale en kg
     * @param maxMassKg Masse maximale en kg
     * @return Liste des payloads dans cette plage
     */
    @Query("SELECT p FROM Payload p WHERE p.massKg BETWEEN :minMass AND :maxMass")
    List<Payload> findByMassRange(
            @Param("minMass") Double minMassKg,
            @Param("maxMass") Double maxMassKg);

    /**
     * Calcule la masse totale de tous les payloads
     *
     * @return Masse totale en kg
     */
    @Query("SELECT SUM(p.massKg) FROM Payload p WHERE p.massKg IS NOT NULL")
    Double calculateTotalMass();

    /**
     * Calcule la masse moyenne des payloads
     *
     * @return Masse moyenne en kg
     */
    @Query("SELECT AVG(p.massKg) FROM Payload p WHERE p.massKg IS NOT NULL")
    Double calculateAverageMass();

    /**
     * Compte les payloads par type
     *
     * @param type Type de payload
     * @return Nombre de payloads de ce type
     */
    long countByType(String type);

    /**
     * Compte les payloads par orbite
     *
     * @param orbit Orbite
     * @return Nombre de payloads sur cette orbite
     */
    long countByOrbit(String orbit);

    /**
     * Trouve tous les types de payloads distincts
     *
     * @return Liste des types uniques
     */
    @Query("SELECT DISTINCT p.type FROM Payload p WHERE p.type IS NOT NULL ORDER BY p.type")
    List<String> findAllDistinctTypes();

    /**
     * Trouve toutes les orbites distinctes
     *
     * @return Liste des orbites uniques
     */
    @Query("SELECT DISTINCT p.orbit FROM Payload p WHERE p.orbit IS NOT NULL ORDER BY p.orbit")
    List<String> findAllDistinctOrbits();

    /**
     * Trouve tous les clients distincts
     *
     * @return Liste des clients uniques
     */
    @Query("SELECT DISTINCT p.customer FROM Payload p WHERE p.customer IS NOT NULL ORDER BY p.customer")
    List<String> findAllDistinctCustomers();
}
