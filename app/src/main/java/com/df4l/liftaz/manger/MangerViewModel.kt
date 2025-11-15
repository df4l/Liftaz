package com.df4l.liftaz.manger

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.df4l.liftaz.data.Repas

class MangerViewModel : ViewModel() {

    val matin = MutableLiveData<List<Repas>>(emptyList())
    val midi = MutableLiveData<List<Repas>>(emptyList())
    val apresMidi = MutableLiveData<List<Repas>>(emptyList())
    val soir = MutableLiveData<List<Repas>>(emptyList())
}
