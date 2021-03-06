package com.quijotelui.electronico.ejecutar

import com.quijotelui.electronico.correo.EnviarCorreo
import com.quijotelui.electronico.util.TipoComprobante
import com.quijotelui.electronico.xml.GeneraFactura
import com.quijotelui.electronico.xml.GeneraGuia
import com.quijotelui.electronico.xml.GeneraNotaCredito
import com.quijotelui.electronico.xml.GeneraRetencion
import com.quijotelui.model.Electronico
import com.quijotelui.service.*
import com.quijotelui.ws.definicion.AutorizacionEstado
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud
import ec.gob.sri.comprobantes.ws.Comprobante
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

class Electronica(val codigo : String, val numero : String, val parametroService : IParametroService, val key : String) {

    var claveAcceso : String? = null
    private var facturaService : IFacturaService? = null
    private var retencionService : IRetencionService? = null
    private var notaCreditoService : INotaCreditoService? = null
    private var guiaService : IGuiaService? = null
    private var electronicoService : IElectronicoService? = null

    constructor(facturaService : IFacturaService,
                codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                electronicoService : IElectronicoService)
            : this(codigo, numero, parametroService, key) {
        this.facturaService = facturaService
        this.electronicoService = electronicoService
    }

    constructor(retencionService : IRetencionService,
                codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                electronicoService : IElectronicoService)
            : this(codigo, numero, parametroService, key) {
        this.retencionService = retencionService
        this.electronicoService = electronicoService
    }

    constructor(guiaService: IGuiaService,
                codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                electronicoService : IElectronicoService)
            : this(codigo, numero, parametroService, key) {
        this.guiaService = guiaService
        this.electronicoService = electronicoService
    }

    constructor(notaCreditoService: INotaCreditoService,
                codigo : String,
                numero : String,
                parametroService : IParametroService,
                key: String,
                electronicoService : IElectronicoService)
            : this(codigo, numero, parametroService, key) {
        this.notaCreditoService = notaCreditoService
        this.electronicoService = electronicoService
    }

    fun enviar(tipo : TipoComprobante) : String {

        if (tipo == TipoComprobante.FACTURA) {
            val genera = GeneraFactura(this.facturaService!!, this.codigo, this.numero)
            this.claveAcceso = genera.xml()
        }
        else if (tipo == TipoComprobante.RETENCION) {
            val genera = GeneraRetencion(this.retencionService!!, this.codigo, this.numero)
            this.claveAcceso = genera.xml()
        }
        else if (tipo == TipoComprobante.NOTA_CREDITO) {
            val genera = GeneraNotaCredito(this.notaCreditoService!!, this.codigo, this.numero)
            this.claveAcceso = genera.xml()
        }
        else if (tipo == TipoComprobante.GUIA) {
            val genera = GeneraGuia(this.guiaService!!, this.codigo, this.numero)
            this.claveAcceso = genera.xml()
        }

        if (this.claveAcceso == ""){
            println("Error al generar la Clave de Acceso")
            return ""
        }

        val procesar = ProcesarElectronica(parametroService, key)
        var respuestaEstado = ""
        if (procesar.firmar(this.claveAcceso!!)) {

            val respuesta = procesar.enviar(this.claveAcceso!!)
            respuesta?.let {
                respuestaEstado = grabarRespuestaEnvio(it)
            }

            procesar.imprimirPDF(this.claveAcceso!!, "", "", tipo)
        }

        return respuestaEstado

    }

    fun comprobar(informacionService : IInformacionService, tipo: TipoComprobante) {

        this.claveAcceso = generaClaveAcceso(tipo)

        val procesar = ProcesarElectronica(parametroService, key)

        val autorizacionEstado = procesar.comprobar(this.claveAcceso!!)

        grabarAutorizacion(autorizacionEstado)

        procesar.imprimirPDF(this.claveAcceso!!,
                autorizacionEstado.autorizacion.numeroAutorizacion,
                autorizacionEstado.autorizacion.fechaAutorizacion?.toString(),
                tipo)

        println("Estado de ${codigo} ${numero} para envío al correo: ${autorizacionEstado.autorizacion.estado}")
        if (autorizacionEstado.autorizacion.estado == "AUTORIZADO"){
            if (tipo == TipoComprobante.FACTURA) {
                val correo = EnviarCorreo(codigo, numero, parametroService, key, informacionService, facturaService!!)
                correo.enviar(tipo)
            }
            else if (tipo == TipoComprobante.RETENCION) {
                val correo = EnviarCorreo(codigo, numero, parametroService, key, informacionService, retencionService!!)
                correo.enviar(tipo)
            }
            else if (tipo == TipoComprobante.NOTA_CREDITO) {
                val correo = EnviarCorreo(codigo, numero, parametroService, key, informacionService, notaCreditoService!!)
                correo.enviar(tipo)
            }
        }
    }

    fun generaClaveAcceso(tipo : TipoComprobante) :String {
        if (tipo == TipoComprobante.FACTURA) {
            val genera = GeneraFactura(this.facturaService!!, this.codigo, this.numero)
            return genera.claveAcceso.toString()
        }
        else if (tipo == TipoComprobante.RETENCION) {
            val genera = GeneraRetencion(this.retencionService!!, this.codigo, this.numero)
            return genera.claveAcceso.toString()
        }
        else if (tipo == TipoComprobante.NOTA_CREDITO) {
            val genera = GeneraNotaCredito(this.notaCreditoService!!, this.codigo, this.numero)
            return genera.claveAcceso.toString()
        }
        else if (tipo == TipoComprobante.GUIA) {
            val genera = GeneraGuia(this.guiaService!!, this.codigo, this.numero)
            return genera.claveAcceso.toString()
        }

        return ""
    }

    private fun grabarRespuestaEnvio(respuesta : RespuestaSolicitud) : String {

        var comprobante: Comprobante
        var electronico = Electronico()
        val fecha = LocalDateTime.now()
        var mensaje = "$fecha |"

        if (respuesta.comprobantes == null) {
            return "RESPUESTA NULA"
        }

        if (respuesta.comprobantes.comprobante.size > 0) {
            for (i in respuesta.comprobantes.comprobante.indices) {
                comprobante = respuesta.comprobantes.comprobante[i] as ec.gob.sri.comprobantes.ws.Comprobante
//                mensaje = mensaje + " " + comprobante.claveAcceso + ": "
                for (m in comprobante.mensajes.mensaje.indices) {
                    val mensajeRespuesta = comprobante.mensajes.mensaje[m]
                    if (mensajeRespuesta.mensaje != null) {
                        mensaje = mensaje + " " + mensajeRespuesta.mensaje + " " + mensajeRespuesta.informacionAdicional
                    }
                }
                mensaje += " "
            }
        }


        if (mensaje.equals("RECIBIDA")){
            mensaje = "$mensaje Conexión exitosa"
        }
        // Si no existe en la base de datos se inserta
        if (this.electronicoService!!.findByComprobante(this.codigo, this.numero).isEmpty()) {

            electronico.codigo = this.codigo
            electronico.numero = this.numero
            electronico.observacion = mensaje
            electronico.estado = respuesta.estado

            this.electronicoService?.saveElectronico(electronico)
        }
        // Si existe en la base de datos se actualiza
        else{
            var electronicoUpdate = this.electronicoService!!.findByComprobante(this.codigo, this.numero)

            for (e in electronicoUpdate.indices) {
                electronico = electronicoUpdate[e]
            }

            electronico.observacion = mensaje + " | " + electronico.observacion
            electronico.estado = respuesta.estado

            this.electronicoService!!.updateElectronico(electronico)
        }

        return respuesta.estado
    }

    private fun grabarAutorizacion(autorizacionEstado : AutorizacionEstado) {

        var electronico = Electronico()
        var fecha : String? = null
        var mensaje = ""

        //            Mejorar
//            print("Estado autorización: " + autorizacionEstado.estadoAutorizacion.descripcion)
        if (autorizacionEstado.autorizacion.mensajes != null) {
            for (m in autorizacionEstado.autorizacion.mensajes.mensaje.indices) {
                val mensajeRespuesta = autorizacionEstado.autorizacion.mensajes.mensaje[m]
                if (mensajeRespuesta.mensaje != null) {
                    mensaje = mensaje + " " + mensajeRespuesta.mensaje + " " + mensajeRespuesta.informacionAdicional
                }
            }
        }

        // Si no existe en la base de datos se inserta
        if (this.electronicoService!!.findByComprobante(this.codigo, this.numero).isEmpty()) {

            electronico.codigo = this.codigo
            electronico.numero = this.numero
            electronico.observacion = " | ${autorizacionEstado.autorizacion.numeroAutorizacion} " +
                    "${autorizacionEstado.autorizacion.fechaAutorizacion} "
            electronico.numeroAutorizacion = autorizacionEstado.autorizacion.numeroAutorizacion
            electronico.estado = autorizacionEstado.autorizacion.estado


            fecha = autorizacionEstado.autorizacion.fechaAutorizacion.year.toString() + "-" +
                    autorizacionEstado.autorizacion.fechaAutorizacion.month.toString() +  "-" +
                    autorizacionEstado.autorizacion.fechaAutorizacion.day.toString() + " " +
                    autorizacionEstado.autorizacion.fechaAutorizacion.hour.toString() + ":" +
                    autorizacionEstado.autorizacion.fechaAutorizacion.minute.toString()

            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val fechaInDateType : Date
            fechaInDateType = simpleDateFormat.parse(fecha)
            println("Fecha autorización: $fechaInDateType")

            electronico.fechaAutorizacion = fechaInDateType

            this.electronicoService!!.saveElectronico(electronico)
            println("Guardado ${electronico.codigo} ${electronico.numero}")
        }
        // Si existe en la base de datos se actualiza
        else {
            var electronicoUpdate = this.electronicoService!!.findByComprobante(this.codigo, this.numero)

            for (e in electronicoUpdate.indices) {
                electronico = electronicoUpdate[e]
            }

            electronico.observacion = mensaje + electronico.observacion

            if (autorizacionEstado.autorizacion.fechaAutorizacion!=null) {

                electronico.observacion = " | ${autorizacionEstado.autorizacion.numeroAutorizacion} " +
                        "${autorizacionEstado.autorizacion.fechaAutorizacion} " + electronico.observacion
                electronico.numeroAutorizacion = autorizacionEstado.autorizacion.numeroAutorizacion


                fecha = autorizacionEstado.autorizacion.fechaAutorizacion.year.toString() + "-" +
                        autorizacionEstado.autorizacion.fechaAutorizacion.month.toString() + "-" +
                        autorizacionEstado.autorizacion.fechaAutorizacion.day.toString() + " " +
                        autorizacionEstado.autorizacion.fechaAutorizacion.hour.toString() + ":" +
                        autorizacionEstado.autorizacion.fechaAutorizacion.minute.toString()

                electronico.estado = autorizacionEstado.autorizacion.estado

                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val fechaInDateType: Date
                fechaInDateType = simpleDateFormat.parse(fecha)
                println("Fecha autorización: $fechaInDateType - Fecha Cruda : ${autorizacionEstado.autorizacion.fechaAutorizacion}")

                electronico.fechaAutorizacion = fechaInDateType

            }

            this.electronicoService!!.updateElectronico(electronico)
            println("Guardado ${electronico.codigo} ${electronico.numero}")
        }
    }
}