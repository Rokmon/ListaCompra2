package com.mispruebas.listacompra2.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


//este es el segundo componente para usar Room
//esta clase es la que se encarga de hacer las consultas a la base de datos
@Dao//esta anotacion es para que Room sepa que es un Dao
interface ItemDao {
    @Query("SELECT * FROM item ORDER BY comprada ASC, nombre ASC")//esta anotacion es para que Room sepa que es una consulta
    fun getAllItems(): List<Item>//esta es la consulta

    @Query("SELECT COUNT(*) FROM item ")//esta anotacion es para que Room sepa que es una consulta
    fun countAll(): Int//esta es la consulta

    @Query("DELETE FROM item")//esta anotacion es para que Room sepa que es una consulta
    suspend fun deleteAll()//esta es la consulta

    @Insert
    fun insertItem(item: Item): Long//esta es la insercion

    @Update
    fun updateItem(item: Item)//esta es la actualizacion

    @Delete
    fun deleteItem(item: Item) //esta es la eliminacion
}