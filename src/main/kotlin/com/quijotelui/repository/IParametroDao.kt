package com.quijotelui.repository

import com.quijotelui.model.Contribuyente
import com.quijotelui.model.Parametro

interface IParametroDao {

    fun findAll() : MutableList<Parametro>
    fun findByNombre(nombre : String) : MutableList<Parametro>
    fun findByTipo(tipo : String) : MutableList<Parametro>
    fun findContribuyente(): MutableList<Contribuyente>
}