package com.quijotelui.controller

import com.quijotelui.electronico.util.Parametros
import com.quijotelui.service.IParametroService
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.quijotelui.HistoriaCertificado
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Security

@RestController
@RequestMapping("/rest/v1")
class CertificadoRestApi {

    @Autowired
    lateinit var parametroService : IParametroService

    @Value("\${key.property}")
    lateinit var keyProperty: String

    @CrossOrigin(value = "*")
    @GetMapping("/certificado")
    fun getCertificadoInformacion() : ResponseEntity<Any> {

        Security.addProvider(BouncyCastleProvider())

        val keyStoreHistory = HistoriaCertificado()
        val pathElectronica = Parametros.getRuta(parametroService.findByNombre("Firma Electrónica"))
        val keyElectronica = Parametros.getClaveElectronica(
                parametroService.findByNombre("Clave Firma Electrónica"), keyProperty)

        val k = keyStoreHistory.getInformacion(pathElectronica, keyElectronica)

        return ResponseEntity(k, HttpStatus.OK)
    }
}