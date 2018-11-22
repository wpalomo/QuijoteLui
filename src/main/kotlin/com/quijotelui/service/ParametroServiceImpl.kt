package com.quijotelui.service

import com.quijotelui.model.Contribuyente
import com.quijotelui.model.Parametro
import com.quijotelui.repository.IParametroDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ParametroServiceImpl : IParametroService {

    @Autowired
    lateinit var parametroDao : IParametroDao


    override fun findAll(): MutableList<Parametro> {
        return parametroDao.findAll()
    }

    override fun findByNombre(nombre: String): MutableList<Parametro> {
        return parametroDao.findByNombre(nombre)
    }

    override fun findByTipo(tipo: String): MutableList<Parametro> {
        return parametroDao.findByTipo(tipo)
    }

    override fun findContribuyente(): MutableList<Contribuyente> {
        return parametroDao.findContribuyente()
    }
}