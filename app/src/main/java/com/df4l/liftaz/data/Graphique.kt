package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "graphiques")
data class Graphique(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var titre: String,
    var typeAbscisse: TypeAbscisse, // Ex: 7 derniers jours, 30 derniers jours, etc.
    var jeuxDeDonnees: List<JeuDeDonneesParam> // La liste des courbes à afficher
)

// --- Éléments pour la flexibilité ---

// Représente une courbe sur le graphique
data class JeuDeDonneesParam(
    val type: TypeJeuDeDonnees,
    val exerciceId: Int? = null, // Optionnel, pour les perfs sur un exercice
    val couleur: Int // Stocker la couleur choisie par l'utilisateur
)

// Enum pour le type de données (Y-axis) -> Très facile à étendre !
enum class TypeJeuDeDonnees {
    POIDS_UTILISATEUR,
    BODYFAT,
    CALORIES_CONSOMMEES,
    PROTEINES_CONSOMMEES,
    PERFORMANCE_EXERCICE_VOLUME, // Volume = séries * reps * poids
    PERFORMANCE_EXERCICE_MAX_POIDS, // 1RM ou juste le poids max soulevé
    // Ajoutez ici de futurs types de données...
}

// Enum pour la durée (X-axis)
enum class TypeAbscisse {
    SEPT_DERNIERS_JOURS,
    TRENTE_DERNIERS_JOURS,
    TROIS_DERNIERS_MOIS,
    DEPUIS_LE_DEBUT
}