/*
 * Created by Evgeniya Zemlyanaya (@zzemlyanaya), ZZen Studio
 *  * Copyright (c) 2021 . All rights reserved.
 */

package ru.zzenstudio.monochrome

import android.app.Application
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.zzenstudio.monochrome.database.AppDatabase
import ru.zzenstudio.monochrome.database.Colour
import kotlin.coroutines.CoroutineContext


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository by lazy {
        Repository.getInstance(
            PreferenceManager.getDefaultSharedPreferences(application),
            AppDatabase.getDatabase(application.applicationContext).colourDao()
        )
    }

    private val parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default
    private val scope = CoroutineScope(coroutineContext)

    val colours = repository.allColours

    fun insert(colour: Colour) = scope.launch {
        repository.insert(colour)
    }

    fun delete(colour: Colour) = scope.launch {
        repository.delete(colour)
    }

    fun edit(oldKey: Int, newName: String) = scope.launch {
        repository.edit(oldKey, newName)
    }

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