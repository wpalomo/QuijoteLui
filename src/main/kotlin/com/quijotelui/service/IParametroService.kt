package com.quijotelui.service

import com.quijotelui.model.Contribuyente
import com.quijotelui.model.Parametro

interface IParametroService {

    fun findAll() : MutableList<Parametro>
    fun findByNombre(nombre : String) : MutableList<Parametro>
    fun findByTipo(tipo : String) : MutableList<Parametro>
    fun findContribuyente(): MutableList<Contribuyente>

}