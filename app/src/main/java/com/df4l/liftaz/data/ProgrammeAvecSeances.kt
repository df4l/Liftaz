package com.df4l.liftaz.data

import androidx.room.*

data class ProgrammeAvecSeances(
    @Embedded val programme: Programme,
    @Relation(
        parentColumn = "id",
        entityColumn = "idProgramme"
    )
    val seances: List<Seance>
)
