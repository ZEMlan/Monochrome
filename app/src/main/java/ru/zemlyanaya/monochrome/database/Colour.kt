package ru.zemlyanaya.monochrome.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "colours")
data class Colour(
    @PrimaryKey
    var code: Int,
    @ColumnInfo
    val hex: String,
    @ColumnInfo
    val name: String
) : Serializable