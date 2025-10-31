package com.df4l.liftaz.pousser.elastiques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.df4l.liftaz.data.ElastiqueDao

class ElastiqueViewModelFactory(
    private val dao: ElastiqueDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElastiqueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ElastiqueViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}