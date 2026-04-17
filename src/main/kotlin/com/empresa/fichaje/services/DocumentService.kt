package com.empresa.fichaje.services

import com.empresa.fichaje.database.tables.DocumentosTable
import com.empresa.fichaje.database.tables.UsuariosTable
import com.empresa.fichaje.dto.request.DocumentRequest
import com.empresa.fichaje.dto.response.DocumentResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DocumentService {

    private val baseUrl =
        System.getenv("BASE_URL")
            ?: "http://localhost:8080/uploads/"

    private fun construirUrlCompleta(
        ruta: String
    ): String = "$baseUrl$ruta"


    fun createDocument(
        request: DocumentRequest
    ) = transaction {

        DocumentosTable.insert {

            it[userId] = request.userId
            it[nombre] = request.nombre
            it[tipo] = request.tipo
            it[url] = request.url
        }
    }


    fun getDocuments(
        userId: Int? = null,
        tipo: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<DocumentResponse> = transaction {

        val query = DocumentosTable
            .innerJoin(UsuariosTable)
            .selectAll()
            .apply {

                userId?.let {

                    andWhere {
                        DocumentosTable.userId eq it
                    }
                }

                tipo?.let {

                    andWhere {
                        DocumentosTable.tipo eq it
                    }
                }
            }


        val sortColumn =
            when (sortBy) {

                "nombre" -> DocumentosTable.nombre
                "tipo" -> DocumentosTable.tipo
                "usuario" -> UsuariosTable.username
                else -> DocumentosTable.id
            }


        val sortOrder =
            if (order == "desc")
                SortOrder.DESC
            else
                SortOrder.ASC


        query
            .orderBy(sortColumn to sortOrder)
            .map {

                DocumentResponse(
                    id = it[DocumentosTable.id],
                    userId = it[DocumentosTable.userId],
                    username = it[UsuariosTable.username],
                    nombre = it[DocumentosTable.nombre],
                    tipo = it[DocumentosTable.tipo],
                    url = construirUrlCompleta(
                        it[DocumentosTable.url]
                    )
                )
            }
    }


    fun deleteDocument(
        id: Int
    ) = transaction {

        val rutaArchivo =
            DocumentosTable
                .select(DocumentosTable.url)
                .where { DocumentosTable.id eq id }
                .firstOrNull()
                ?.get(DocumentosTable.url)


        rutaArchivo?.let {

            val archivo =
                File("uploads/$it")

            if (archivo.exists())
                archivo.delete()
        }


        DocumentosTable.deleteWhere {

            DocumentosTable.id eq id
        }
    }
}