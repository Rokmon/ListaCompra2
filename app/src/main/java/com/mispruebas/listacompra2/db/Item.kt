package com.mispruebas.listacompra2.db

import androidx.room.Entity
import androidx.room.PrimaryKey

//este es el primer componente para usar Room
@Entity(tableName = "item")//esta anotacion es para que Room sepa que es una tabla
data class Item (
    @PrimaryKey(autoGenerate = true)//esta anotacion es para que Room sepa que es la clave primaria
    val id       : Int = 0,//esta es la clave primaria
    var nombre     : String,//este es el primer campo
    var comprada : Boolean//este es el segundo campo
)