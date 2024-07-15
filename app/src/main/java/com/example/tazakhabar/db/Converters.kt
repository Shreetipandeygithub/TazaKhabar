package com.example.tazakhabar.db

import androidx.room.TypeConverter
import com.example.tazakhabar.model.Source

//created after creating DAO
class Converters {

    @TypeConverter
    fun fromSource(source: Source):String{
        return source.name
    }

    @TypeConverter
    fun toSource(name: String):Source{
        return Source(name,name)
    }

}