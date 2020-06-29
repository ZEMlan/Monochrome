package ru.zemlyanaya.monochrome.database

import android.content.SharedPreferences

class Repository(private val sharedPrefs: SharedPreferences) {

    fun getLastColour() = sharedPrefs.getInt(COLOUR, 0x000000)

    fun setNewColour(colourCode: Int){
        sharedPrefs.edit().putInt(COLOUR, colourCode).apply()
    }

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: Repository? = null

        //fun getInstance(sharedPrefs: SharedPreferences, colourDao: ColourDao) =
        fun getInstance(sharedPrefs: SharedPreferences) =
            instance ?: synchronized(this) {
                //instance ?: Repository(sharedPrefs, colourDao).also { instance = it }
                instance
                    ?: Repository(sharedPrefs)
                        .also { instance = it }
            }

        const val COLOUR = "colour"
    }
}