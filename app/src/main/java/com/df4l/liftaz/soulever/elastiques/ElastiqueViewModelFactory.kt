package com.df4l.liftaz.soulever.elastiques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.df4l.liftaz.data.ElastiqueDao
import com.df4l.liftaz.data.SerieDao

class ElastiqueViewModelFactory(
    private val elastiqueDao: ElastiqueDao,
    private val serieDao: SerieDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElastiqueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ElastiqueViewModel(elastiqueDao, serieDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}