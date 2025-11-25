package com.df4l.liftaz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PeriodeRepas {
    MATIN,
    MIDI,
    APRES_MIDI,
    SOIR
}

enum class TypeElement {
    ALIMENT,
    RECETTE
}


@Entity(tableName = "diete_elements")
data class DieteElements(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var idDiete: Int,
    var idElement: Int,
    var typeElement: TypeElement,
    var periodeRepas: PeriodeRepas,
    var quantiteGrammes: Int
)
