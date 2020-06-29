package ru.zemlyanaya.monochrome

import android.app.Application
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.zemlyanaya.monochrome.database.Repository


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository by lazy { Repository.getInstance(PreferenceManager.getDefaultSharedPreferences(application))  }

    private val _colour = MutableLiveData(repository.getLastColour())
    private val _monochrome = MutableLiveData("Mono")
    private val _mono =  MutableLiveData(true)

    val colour: LiveData<Int> = _colour
    val monochrome: LiveData<String> = _monochrome
    val mono: LiveData<Boolean> = _mono

    fun onMonochrome(){
        if(_mono.value == true) {
            _monochrome.value = "Chrome"
            _mono.value = false
        }
        else {
            _monochrome.value = "Mono"
            _mono.value = true
        }
    }

    fun setNewColour(colourCode: Int){
        _colour.value = colourCode
        repository.setNewColour(colourCode)
    }
}