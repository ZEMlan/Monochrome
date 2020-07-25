package ru.zemlyanaya.monochrome.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface IColourDao {
    @Query("SELECT * FROM colours")
    fun getAll() : LiveData<List<Colour>?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(colour: Colour)

    @Delete
    fun delete(colour: Colour)

    @Query("UPDATE colours SET name=:newName WHERE `code`=:oldKey")
    fun edit(oldKey: Int, newName: String)
}