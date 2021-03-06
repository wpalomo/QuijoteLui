package com.quijotelui.repository

import com.quijotelui.model.FacturaDetalle

interface IFacturaDetalleDao {

    fun findAll() : MutableList<FacturaDetalle>
    fun findByComprobante(codigo : String, numero : String) : MutableList<FacturaDetalle>

}