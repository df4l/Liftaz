package com.df4l.liftaz.manger

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.df4l.liftaz.data.MangerHistorique

class MangerViewModel : ViewModel() {

    val matin = MutableLiveData<List<MangerHistorique>>(emptyList())
    val midi = MutableLiveData<List<MangerHistorique>>(emptyList())
    val apresMidi = MutableLiveData<List<MangerHistorique>>(emptyList())
    val soir = MutableLiveData<List<MangerHistorique>>(emptyList())
}
