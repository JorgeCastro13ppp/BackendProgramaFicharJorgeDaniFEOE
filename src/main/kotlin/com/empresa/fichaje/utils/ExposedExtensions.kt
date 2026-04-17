package com.empresa.fichaje.utils

import org.jetbrains.exposed.sql.*

fun ColumnSet.selectWhere(
    where: SqlExpressionBuilder.() -> Op<Boolean>
): Query {

    return this
        .selectAll()
        .where(where)
}

fun ColumnSet.selectOneWhere(
    where: SqlExpressionBuilder.() -> Op<Boolean>
): Query {

    return this
        .selectAll()
        .where(where)
        .limit(1)
}