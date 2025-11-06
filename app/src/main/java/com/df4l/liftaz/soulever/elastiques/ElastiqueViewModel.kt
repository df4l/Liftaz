package com.df4l.liftaz.soulever.elastiques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.df4l.liftaz.data.Elastique
import com.df4l.liftaz.data.ElastiqueDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ElastiqueViewModel(private val dao: ElastiqueDao) : ViewModel() {

    private val _elastiques = MutableStateFlow<List<Elastique>>(emptyList())
    val elastiques: StateFlow<List<Elastique>> get() = _elastiques

    init {
        loadElastiques()
    }

    fun loadElastiques() {
        viewModelScope.launch {
            _elastiques.value = dao.getAll()
        }
    }

    fun insert(elastique: Elastique) {
        viewModelScope.launch {
            dao.insert(elastique)
            loadElastiques()
        }
    }

    // Ajout : insertion multiple en une fois
    fun insertAll(elastiques: List<Elastique>) {
        viewModelScope.launch {
            dao.insertAll(elastiques)
            loadElastiques()
        }
    }

    fun updateBitmasks(newList: List<Elastique>) {
        viewModelScope.launch {
            newList.forEachIndexed { index, e ->
                val updated = e.copy(valeurBitmask = 1 shl index)
                dao.update(updated)
            }
            _elastiques.value = dao.getAll()
        }
    }

    fun delete(elastique: Elastique) {
        viewModelScope.launch {
            dao.delete(elastique)
            loadElastiques()
        }
    }

    // Optionnel : expose une méthode pour connaître le count (utilisable depuis le fragment)
    suspend fun countSync(): Int = dao.count()
}
