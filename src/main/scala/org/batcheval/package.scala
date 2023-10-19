package org

import org.apache.spark.sql.Row

package object batcheval{
    case class Employee(
        name: String,
        position: String,
        rating: Int,
        seniority: Float)

    object Employee {
        def apply(row: Row): Employee =
            Employee(
                row.getString(0),
                row.getString(1),
                row.getString(2).toInt,
                row.getString(3).toFloat
            )
    }
}
