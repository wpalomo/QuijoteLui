package com.quijotelui.subscription

import com.quijotelui.electronico.util.Parametros
import com.quijotelui.service.IParametroService

class Subscription(val parametroService : IParametroService) {

    fun isAlive(key : String) : Boolean {
        println("Key Property: $key")
        val dateSubscription = Parametros.getSuscripcion(
                parametroService.findByNombre("Suscripción (aaaa-mm-dd)"), key)
        println("Fecha final de suscripción: $dateSubscription")
        return false
    }
}