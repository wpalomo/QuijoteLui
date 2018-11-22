package com.quijotelui.electronico.correo

import com.quijotelui.electronico.util.Parametros
import com.quijotelui.electronico.util.TipoComprobante
import com.quijotelui.electronico.xml.GeneraFactura
import com.quijotelui.electronico.xml.GeneraNotaCredito
import com.quijotelui.electronico.xml.GeneraRetencion
import com.quijotelui.model.Informacion
import com.quijotelui.service.*
import org.apache.commons.mail.EmailException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.io.File
import java.nio.charset.StandardCharsets
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class EnviarCorreo(val codigo : String,
                   val numero : String,
                   val parametroService : IParametroService,
                   val key : String,
                   val informacionService : IInformacionService) {

    var documento : String = ""
    private var facturaService : IFacturaService? = null
    private var retencionService : IRetencionService? = null
    private var notaCreditoService : INotaCreditoService? = null

    constructor(codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                informacionService : IInformacionService,
                facturaService : IFacturaService)
            : this(codigo, numero, parametroService, key, informacionService) {
        this.facturaService = facturaService
    }

    constructor(codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                informacionService : IInformacionService,
                retencionService : IRetencionService)
            : this(codigo, numero, parametroService, key, informacionService) {
        this.retencionService = retencionService
    }

    constructor(codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                informacionService : IInformacionService,
                notaCreditoService : INotaCreditoService)
            : this(codigo, numero, parametroService, key, informacionService) {
        this.notaCreditoService = notaCreditoService
    }

    private fun getClaveAcceso(tipo : TipoComprobante) : String {

        when (tipo) {
            TipoComprobante.FACTURA -> {
                val factura = facturaService!!.findByComprobante(codigo, numero)
                if (factura.isEmpty()) {
                    return ""
                }
                this.documento = factura[0].documento!!

                val genera = GeneraFactura(this.facturaService!!, this.codigo, this.numero)
                return genera.claveAcceso.toString()
            }
            TipoComprobante.RETENCION -> {
                val retencion = retencionService!!.findByComprobante(codigo, numero)
                if (retencion.isEmpty()) {
                    return ""
                }
                this.documento = retencion[0].documento!!

                val genera = GeneraRetencion(this.retencionService!!, this.codigo, this.numero)
                return genera.claveAcceso.toString()
            }
            TipoComprobante.NOTA_CREDITO -> {
                val notaCredito = notaCreditoService!!.findByComprobante(codigo, numero)
                if (notaCredito.isEmpty()) {
                    return ""
                }
                this.documento = notaCredito[0].documento!!

                val genera = GeneraNotaCredito(this.notaCreditoService!!, this.codigo, this.numero)
                return genera.claveAcceso.toString()
            }
            else -> return ""
        }

    }

    fun isValidEmailAddress(email : String): Boolean {

        try {
            val emailAddr = InternetAddress(email)
            emailAddr.validate()
        } catch (ex: AddressException) {
            return false
        }

        return true

    }

    fun enviar(tipo : TipoComprobante) : ResponseEntity<MutableList<Informacion>> {

        val descripcion : String = translateTipo(tipo.toString())

        val claveAcceso : String = getClaveAcceso(tipo)
        if (claveAcceso == "") {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        println("Clave de Acceso: " + claveAcceso)

        val informacion = informacionService.correoByDocumento(this.documento)

        if (informacion.isEmpty()) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }

        val parametro = parametroService.findAll()
        val datosCorreo = Parametros.getDatosCorreo(parametro, key)

        val rutaPDF = Parametros.getRuta(parametroService.findByNombre("PDF"))
        val rutaXML = Parametros.getRuta(parametroService.findByNombre("Autorizado"))
        val rutaTemplateHTML = Parametros.getRuta(parametroService.findByNombre("Correo Plantilla"))
        val rutaLogo = Parametros.getRuta(parametroService.findByNombre("Logo"))

        val contribuyenteRemitente = parametroService.findContribuyente()

        try {
            val correo = Correo(datosCorreo.servidor, datosCorreo.puerto)

            correo.remitente(datosCorreo.correo,
                    contribuyenteRemitente[0].nombreComercial!!,
                    datosCorreo.clave)

            correo.pdf(File(rutaPDF +
                    File.separatorChar +
                    "$claveAcceso.pdf"), "$descripcion PDF")
            correo.xml(File(rutaXML +
                    File.separatorChar +
                    "$claveAcceso.xml"), "$descripcion XML")

            for (i in informacion.indices) {
                println("Informaciones: ${informacion[i].nombre} - ${informacion[i].valor}")
                if (!isValidEmailAddress(informacion[i].valor.toString())) {
                        println("Correo no válido: ${informacion[i].valor.toString()}")
                        return ResponseEntity(HttpStatus.OK)
                    }
                    correo.destinatario(informacion[i].valor.toString())
            }
//            correo.enviar("$descripcion Electrónica",
//                    "Saludos cordiales, Adjunto el comprobante electrónico")
            correo.enviar("$descripcion Electrónica", getMensajeHTML(rutaTemplateHTML), rutaLogo)

        }
        catch (e : java.lang.IllegalArgumentException) {
            println("Error al enviar el correo ${e.message}")
            return ResponseEntity(HttpStatus.OK)
        }
        catch (e: javax.mail.AuthenticationFailedException){
            println("Error al enviar el correo ${e.message}")
            return ResponseEntity(HttpStatus.OK)
        }
        catch (e: java.io.IOException){
            println("Error al enviar el correo ${e.message}")
            return ResponseEntity(HttpStatus.OK)
        }
        catch (e: EmailException){
            println("Error al enviar el correo ${e.message}")
            return ResponseEntity(HttpStatus.OK)
        }

        return ResponseEntity<MutableList<Informacion>>(informacion, HttpStatus.OK)

    }

    fun translateTipo(tipo : String) : String {
        when (tipo) {
            "FACTURA" -> return "Factura"
            "NOTA_CREDITO" -> return "Nota de Crédito"
            "RETENCION" -> return "Retención"
            else -> return ""
        }
    }

    fun getMensajeHTML(ruta : String) : String {
        val encoded = File(ruta).readBytes()
        return String(encoded, StandardCharsets.UTF_8)
    }
}