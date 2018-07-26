package com.quijotelui.subscription

import com.quijotelui.electronico.util.Parametros
import com.quijotelui.service.IParametroService
import java.util.*
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat

class SuscripcionManager(val parametroService : IParametroService) {

    fun isAlive(key : String) : Boolean {
        val dateSubscription = Parametros.getSuscripcion(
                parametroService.findByNombre("Suscripción (aaaa-mm-dd)"), key)

        val diasFinal = Days.daysBetween(DateTime(Date()), DateTime(dateSubscription)).days
        println("Fecha final de suscripción: $dateSubscription")
        println("Días para finalizar la suscripción: $diasFinal")

        if (diasFinal >= 0) {
            return true
        }
        return false
    }

    fun suscripcion(key : String) : Suscripcion {
        var fechaFinal: Date = Parametros.errorDate()
        var diasRestantes = -1
        var suscriptor = ""
        var organizacion = ""

        val parametro = parametroService.findByTipo("SUSCRIPCIÓN")

        if (parametro.size > 0) {
            for (i in parametro.indices) {
                val row = parametro[i]
                if (row.nombre == "Suscripción (aaaa-mm-dd)") {
                    fechaFinal = Parametros.toDate(row.valor!!, key)
                    diasRestantes = Days.daysBetween(DateTime(Date()), DateTime(fechaFinal)).days
                }
                if (row.nombre == "Correo de cuenta") {
                    suscriptor = row.valor!!
                }
                if (row.nombre == "Organización") {
                    organizacion = row.valor!!
                }
            }
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        return Suscripcion(suscriptor,
                organizacion,
                dateFormat.format(fechaFinal),
                "aaaa-mm-dd",
                diasRestantes)
    }
}