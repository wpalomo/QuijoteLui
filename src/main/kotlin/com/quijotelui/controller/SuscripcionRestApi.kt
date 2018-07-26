package com.quijotelui.controller

import com.quijotelui.model.Parametro
import com.quijotelui.service.IParametroService
import com.quijotelui.subscription.SuscripcionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/rest/v1")
class SuscripcionRestApi {

    @Autowired
    lateinit var parametroService : IParametroService

    @Value("\${key.property}")
    lateinit var keyProperty: String

    @CrossOrigin(value = "*")
    @GetMapping("/suscripcion")
    fun getSuscripcion() : ResponseEntity<Any> {

        val manager = SuscripcionManager(parametroService)

        return ResponseEntity(manager.suscripcion(keyProperty), HttpStatus.OK)
    }

}