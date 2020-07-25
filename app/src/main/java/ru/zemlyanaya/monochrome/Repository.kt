package ru.zemlyanaya.monochrome

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ru.zemlyanaya.monochrome.database.Colour
import ru.zemlyanaya.monochrome.database.IColourDao

class Repository(private val sharedPrefs: SharedPreferences, private val colourDao: IColourDao){

    fun getLastColour() = sharedPrefs.getInt(COLOUR, 0x000000)

    fun setNewColour(colourCode: Int){
        sharedPrefs.edit().putInt(COLOUR, colourCode).apply()
    }

    // Room executes all queries on a separate thread.
    val allColours: LiveData<List<Colour>?> = colourDao.getAll()

    fun insert(colour: Colour){
        colourDao.insert(colour)
    }

    fun delete(colour: Colour){
        colourDao.delete(colour)
    }

    fun edit(oldKey: Int, newName: String) {
        colourDao.edit(oldKey, newName)
    }


    companion object {

        // For Singleton instantiation
        @Volatile private var instance: Repository? = null

        fun getInstance(sharedPrefs: SharedPreferences, colourDao: IColourDao) =
            instance ?: synchronized(this) {
                instance ?: Repository(sharedPrefs, colourDao).also { instance = it }
            }

        const val COLOUR = "colour"
    }
}