package com.df4l.liftaz.data

import java.util.Date

data class Progres(
    var date: Date,
    var poidsKg: Float,
    var bodyFatPourcentage: Float? = null,
    var tourDeBide: Float? = null
)
