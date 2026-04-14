package com.empresa.fichaje.services

import com.empresa.fichaje.database.DocumentosTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.DocumentRequest
import com.empresa.fichaje.models.DocumentResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DocumentService {

    private fun construirUrlCompleta(ruta: String): String {

        val baseUrl =
            System.getenv("BASE_URL")
                ?: "http://localhost:8080/uploads/"

        return "$baseUrl$ruta"
    }

    fun createDocument(request: DocumentRequest) {

        transaction {

            DocumentosTable.insert {

                it[userId] = request.userId
                it[nombre] = request.nombre
                it[tipo] = request.tipo
                it[url] = request.url
            }
        }
    }


    fun getDocuments(
        userId: Int? = null,
        tipo: String? = null,
        sortBy: String? = null,
        order: String? = null
    ): List<DocumentResponse> {

        return transaction {

            var query =
                DocumentosTable
                    .innerJoin(
                        UsuariosTable,
                        { DocumentosTable.userId },
                        { UsuariosTable.id }
                    )
                    .selectAll()


            if (userId != null) {

                query =
                    query.andWhere {
                        DocumentosTable.userId eq userId
                    }
            }


            if (tipo != null) {

                query =
                    query.andWhere {
                        DocumentosTable.tipo eq tipo
                    }
            }


            val sortColumn = when (sortBy) {

                "nombre" -> DocumentosTable.nombre
                "tipo" -> DocumentosTable.tipo
                "usuario" -> UsuariosTable.username
                "id" -> DocumentosTable.id

                else -> DocumentosTable.id
            }


            val sortOrder =
                if (order == "desc")
                    SortOrder.DESC
                else
                    SortOrder.ASC


            query =
                query.orderBy(sortColumn to sortOrder)


            query.map {

                DocumentResponse(
                    id = it[DocumentosTable.id],
                    userId = it[DocumentosTable.userId],
                    username = it[UsuariosTable.username],
                    nombre = it[DocumentosTable.nombre],
                    tipo = it[DocumentosTable.tipo],
                    url = construirUrlCompleta(it[DocumentosTable.url])
                )
            }
        }
    }


    fun deleteDocument(id: Int) {

        val rutaArchivo =
            transaction {

                DocumentosTable
                    .selectAll()
                    .where { DocumentosTable.id eq id }
                    .firstOrNull()
                    ?.get(DocumentosTable.url)
            }


        if (rutaArchivo != null) {

            val archivo =
                File("uploads/$rutaArchivo")

            if (archivo.exists()) {

                archivo.delete()
            }
        }


        transaction {

            DocumentosTable.deleteWhere {

                DocumentosTable.id eq id
            }
        }
    }
}