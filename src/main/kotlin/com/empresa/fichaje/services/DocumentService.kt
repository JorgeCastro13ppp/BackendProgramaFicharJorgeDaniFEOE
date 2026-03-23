package com.empresa.fichaje.services

import com.empresa.fichaje.database.DocumentosTable
import com.empresa.fichaje.models.DocumentRequest
import com.empresa.fichaje.models.DocumentResponse
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
                        nombre = it[DocumentosTable.nombre],
                        tipo = it[DocumentosTable.tipo],
                        url = it[DocumentosTable.url]
                    )
                }
        }
    }

    fun getDocumentsByType(userId: Int, tipo: String): List<DocumentResponse> {
        return transaction {
            DocumentosTable.selectAll()
                .filter {
                    it[DocumentosTable.userId] == userId &&
                            it[DocumentosTable.tipo] == tipo
                }
                .map {
                    DocumentResponse(
                        nombre = it[DocumentosTable.nombre],
                        tipo = it[DocumentosTable.tipo],
                        url = it[DocumentosTable.url]
                    )
                }
        }
    }
}