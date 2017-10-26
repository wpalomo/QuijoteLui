package com.quijotelui.repository

import com.quijotelui.model.Factura
import java.util.*

interface IFacturaDao {

    fun findAll() : MutableList<Factura>
    fun findByFecha(fecha : Date) : MutableList<Factura>
    fun findByComprobante(codigo : String, numero : String) : MutableList<Factura>
    fun count(codigo : String, numero : String) : Int

}