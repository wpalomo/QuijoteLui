package com.quijotelui.electronico.ejecutar

import com.quijotelui.clientews.Comprobar
import com.quijotelui.clientews.Enviar
import com.quijotelui.electronico.util.Archivos
import com.quijotelui.electronico.util.Parametros
import com.quijotelui.electronico.util.TipoComprobante
import com.quijotelui.firmador.XAdESBESSignature
import com.quijotelui.printer.pdf.FacturaPDF
import com.quijotelui.printer.pdf.GuiaRemisionPDF
import com.quijotelui.printer.pdf.NotaCreditoPDF
import com.quijotelui.printer.pdf.RetencionPDF
import com.quijotelui.service.IParametroService
import com.quijotelui.ws.definicion.AutorizacionEstado
import com.quijotelui.ws.definicion.Estado
import ec.gob.sri.comprobantes.ws.Comprobante
import ec.gob.sri.comprobantes.ws.Mensaje
import ec.gob.sri.comprobantes.ws.RespuestaSolicitud
import ec.gob.sri.comprobantes.ws.aut.Autorizacion
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProcesarElectronica(val parametroService : IParametroService, val key : String) {

    fun firmar(claveAcceso : String) : Boolean {
        val xadesBesFirma = XAdESBESSignature()
        val rutaGenerado = Parametros.getRuta(parametroService.findByNombre("Generado"))
        val rutaFirmado = Parametros.getRuta(parametroService.findByNombre("Firmado"))
        val rutaFirmaElectronica = Parametros.getRuta(parametroService.findByNombre("Firma Electrónica"))
        val firmaElectronica = Parametros.getClaveElectronica(
                parametroService.findByNombre("Clave Firma Electrónica"), key)

        if (!Archivos.esDirectorio(rutaFirmado) && !Archivos.esArchivo(rutaFirmaElectronica)) {
            return false
        }

        xadesBesFirma.firmar("$rutaGenerado" + "${File.separatorChar}" + "${claveAcceso}.xml",
                "${claveAcceso}.xml",
                "$rutaFirmado",
                "$rutaFirmaElectronica",
                "$firmaElectronica")
        return true
    }

    fun enviar(claveAcceso : String): RespuestaSolicitud? {

        var respuesta = RespuestaSolicitud()
        val direccionWebServiceEnviado = Parametros.getRuta(parametroService.findByNombre("Web Service Recepción"))

        if (!isWSDLAlive(direccionWebServiceEnviado)){
            var comprobante = Comprobante()
            comprobante.claveAcceso = claveAcceso

            var mensaje = Mensaje()
            mensaje.mensaje = "No existe conexión con el Web Service $direccionWebServiceEnviado"
            mensaje.tipo = "ERROR"

            comprobante.mensajes = Comprobante.Mensajes()

            comprobante.mensajes.mensaje.add(mensaje)

            println("Mensajes: ${comprobante.mensajes.mensaje.size}")

            respuesta.comprobantes = RespuestaSolicitud.Comprobantes()
            respuesta.comprobantes.comprobante.add(comprobante)

            respuesta.estado = "ERROR"
            return respuesta
        }

        val rutaFirmado = Parametros.getRuta(parametroService.findByNombre("Firmado"))
        val rutaEnviado= Parametros.getRuta(parametroService.findByNombre("Enviado"))
        val rutaRechazado= Parametros.getRuta(parametroService.findByNombre("Rechazado"))


        val enviar = Enviar(rutaFirmado + File.separatorChar + claveAcceso + ".xml",
                rutaEnviado,
                rutaRechazado,
                direccionWebServiceEnviado)

        respuesta = enviar.executeEnviar()

        println("Estado del comprobante ${claveAcceso} : ${respuesta.estado}")

        return respuesta

    }

    fun comprobar(claveAcceso : String) : AutorizacionEstado {

        var autorizacionEstado = AutorizacionEstado(Autorizacion(), Estado.NO_AUTORIZADO)
        val direccionWebServiceAutorizacion = Parametros.getRuta(parametroService.findByNombre("Web Service Autorización"))

        if (!isWSDLAlive(direccionWebServiceAutorizacion)){
            return autorizacionEstado
        }

        val rutaAutorizado= Parametros.getRuta(parametroService.findByNombre("Autorizado"))
        val rutaNoAutorizado= Parametros.getRuta(parametroService.findByNombre("NoAutorizado"))

        val comprobar = Comprobar(rutaAutorizado,
                rutaNoAutorizado,
                direccionWebServiceAutorizacion)

        autorizacionEstado = comprobar.executeComprobar(claveAcceso)

        println("Estado del comprobante ${claveAcceso} : ${autorizacionEstado.estadoAutorizacion.descripcion}")

        return autorizacionEstado
    }

    /*
    Imprime el comprobante
    Tipo:  Factura, Retención
     */

    fun imprimirPDF(claveAcceso : String, autorizacion : String? = "", fechaAutorizacion : String? = "", tipo : TipoComprobante) {

        val rutaGenerado = Parametros.getRuta(parametroService.findByNombre("Generado"))
        val rutaReportes= Parametros.getRuta(parametroService.findByNombre("Reportes"))
        val logo= Parametros.getRuta(parametroService.findByNombre("Logo"))
        val rutaPDF= Parametros.getRuta(parametroService.findByNombre("PDF"))

        if (!Archivos.esArchivo(rutaGenerado + File.separatorChar + claveAcceso + ".xml")) {
            println("No existe el archivo Generado: $claveAcceso.xml")
            return
        }

        when (tipo) {
            TipoComprobante.FACTURA -> {
                val pdf = FacturaPDF(rutaReportes, logo, rutaPDF)
                pdf.genera(rutaGenerado + File.separatorChar + claveAcceso + ".xml",
                        autorizacion,
                        fechaAutorizacion)
            }
            TipoComprobante.RETENCION -> {
                val pdf = RetencionPDF(rutaReportes, logo, rutaPDF)
                pdf.genera(rutaGenerado + File.separatorChar + claveAcceso + ".xml",
                        autorizacion,
                        fechaAutorizacion)
            }
            TipoComprobante.NOTA_CREDITO -> {
                val pdf = NotaCreditoPDF(rutaReportes, logo, rutaPDF)
                pdf.genera(rutaGenerado + File.separatorChar + claveAcceso + ".xml",
                        autorizacion,
                        fechaAutorizacion)
            }
            TipoComprobante.GUIA -> {
                val pdf = GuiaRemisionPDF(rutaReportes, logo, rutaPDF)
                pdf.genera(rutaGenerado + File.separatorChar + claveAcceso + ".xml",
                        autorizacion,
                        fechaAutorizacion)
            }
        }

    }

    private fun isWSDLAlive(direccionWSDL : String) : Boolean {
        var c: HttpURLConnection? = null
        try {
            val u = URL(direccionWSDL)
            c = u.openConnection() as HttpURLConnection
            c.requestMethod = "GET"
            c.inputStream
            if (c.responseCode == 200) {
                return true
            }
        } catch (e: IOException) {
            println("Error de conexión con WSDL : " + e.message)
            return false
        } finally {
            if (c != null) {
                c.disconnect()
            }
        }
        return false
    }
}