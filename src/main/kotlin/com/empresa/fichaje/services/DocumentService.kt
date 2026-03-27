package com.empresa.fichaje.services

import com.empresa.fichaje.database.DocumentosTable
import com.empresa.fichaje.database.UsuariosTable
import com.empresa.fichaje.models.DocumentRequest
import com.empresa.fichaje.models.DocumentResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DocumentService {

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

    fun getDocuments(userId: Int): List<DocumentResponse> {
        return transaction {
            DocumentosTable.selectAll()
                .filter { it[DocumentosTable.userId] == userId }
                .map {
                    DocumentResponse(
                        id = it[DocumentosTable.id],
                        userId = it[DocumentosTable.userId],
                        username = it[UsuariosTable.username],
                        nombre = it[DocumentosTable.nombre],
                        tipo = it[DocumentosTable.tipo],
                        url = it[DocumentosTable.url]
                    )
                }
        }
    }

    fun getDocumentsFiltered(
        userId: Int? = null,
        tipo: String? = null
    ): List<DocumentResponse> {

        return transaction {

            DocumentosTable
                .innerJoin(
                    UsuariosTable,
                    { DocumentosTable.userId },
                    { UsuariosTable.id }
                )
                .selectAll()
                .filter {

                    (userId == null ||
                            it[DocumentosTable.userId] == userId)

                            &&

                            (tipo == null ||
                                    it[DocumentosTable.tipo] == tipo)
                }
                .map {

                    DocumentResponse(
                        id = it[DocumentosTable.id],
                        userId = it[DocumentosTable.userId],
                        username = it[UsuariosTable.username],
                        nombre = it[DocumentosTable.nombre],
                        tipo = it[DocumentosTable.tipo],
                        url = it[DocumentosTable.url]
                    )
                }
        }
    }
    fun deleteDocument(id: Int) {

        transaction {

            DocumentosTable.deleteWhere {
                DocumentosTable.id eq id
            }
        }
    }
}