package com.empresa.fichaje.database.tables

import org.jetbrains.exposed.sql.Table

object JornadasLaboralesTable : Table("jornadas_laborales") {

    val id = integer("id").autoIncrement()

    val userId =
        reference("user_id", UsuariosTable.id)

    val fecha =
        varchar("fecha", 10) // yyyy-MM-dd


    // timestamps reales detectados
    val entradaReal =
        long("entrada_real").nullable()

    val salidaReal =
        long("salida_real").nullable()


    // timestamps legales tras aplicar reglas empresa
    val entradaLegal =
        long("entrada_legal").nullable()

    val salidaLegal =
        long("salida_legal").nullable()


    // tiempos reales
    val tiempoTrabajoReal =
        long("tiempo_trabajo_real").default(0)

    val tiempoViajeReal =
        long("tiempo_viaje_real").default(0)

    val tiempoDescansoReal =
        long("tiempo_descanso_real").default(0)


    // tiempos legales
    val tiempoLegal =
        long("tiempo_legal").default(0)


    // extra detectado automáticamente
    val tiempoExtraDetectado =
        long("tiempo_extra_detectado").default(0)


    val cerradaAutomaticamente =
        bool("cerrada_automaticamente").default(false)

    val procesada =
        bool("procesada").default(false)


    override val primaryKey =
        PrimaryKey(id)
}