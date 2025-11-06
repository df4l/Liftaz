package com.df4l.liftaz.pousser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PousserViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Y'a littéralement rien pour l'instant"
    }
    val text: LiveData<String> = _text
}