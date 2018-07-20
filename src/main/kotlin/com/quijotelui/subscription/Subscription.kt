package com.quijotelui.subscription

import com.quijotelui.electronico.util.Parametros
import com.quijotelui.service.IParametroService
import java.util.*
import org.joda.time.DateTime
import org.joda.time.Days

class Subscription(val parametroService : IParametroService) {

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
}