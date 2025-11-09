package com.df4l.liftaz.soulever.elastiques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.data.ElastiqueDao
import com.df4l.liftaz.data.SerieDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ElastiqueViewModel(
    private val elastiqueDao: ElastiqueDao,
    private val serieDao: SerieDao
) : ViewModel() {

    private val _elastiques = MutableStateFlow<List<Elastique>>(emptyList())
    val elastiques: StateFlow<List<Elastique>> get() = _elastiques

    init {
        loadElastiques()
    }

    private fun loadElastiques() {
        viewModelScope.launch {
            _elastiques.value = elastiqueDao.getAll()
        }
    }

    fun insert(elastique: Elastique) {
        viewModelScope.launch {
            elastiqueDao.insert(elastique)
            loadElastiques()
        }
    }

    fun insertAll(elastiques: List<Elastique>) {
        viewModelScope.launch {
            elastiqueDao.insertAll(elastiques)
            loadElastiques()
        }
    }

    fun updateBitmasks(newList: List<Elastique>) {
        viewModelScope.launch {
            newList.forEachIndexed { index, e ->
                elastiqueDao.update(e.copy(valeurBitmask = 1 shl index))
            }
            _elastiques.value = elastiqueDao.getAll()
        }
    }

    fun delete(elastique: Elastique) {
        viewModelScope.launch {
            elastiqueDao.deleteAndRecalculate(elastique, serieDao)
            _elastiques.value = elastiqueDao.getAll()
        }
    }

    suspend fun countSync(): Int = elastiqueDao.count()
}

