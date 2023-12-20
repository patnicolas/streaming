package org.pipeline.batch

import org.apache.spark.sql.SparkSession
import org.pipeline.batch.OutlierRemoval.default_validate


/**
 * Vary basic wrapper for removing outlier data
 * @param validate Validate employee entry. A default validation routine is provided  to the
 *                 constructor
 * @param sparkSession Implicit  reference to the current Spark Context
 */
final class OutlierRemoval(validate: Employee => Boolean)(implicit sparkSession: SparkSession){
    def apply(srcFilename: String, destFilename: String): Unit = try  {
      import sparkSession.implicits._

      val employeeDS = sparkSession.read.option("delimiter", ",").csv(srcFilename).map(Employee(_))
      employeeDS.show(5)
      // Filter out the outlier
      val filteredEmployeeDS = employeeDS.filter(default_validate(_))
      filteredEmployeeDS.write.csv(destFilename)
      // Thread.sleep(2000)
    }
    catch {
      case e: Exception => print(s"ERROR: ${e.getMessage}")
    }
}


object OutlierRemoval{
  def apply()(implicit sparkSession: SparkSession): OutlierRemoval = new OutlierRemoval(default_validate)

  private val valid_employee_position = Set[String](
    "intern",
    "engineer",
    "manager",
    "director'",
    "VP"
  )

  private val default_validate = (employee: Employee) => {
    valid_employee_position.contains(employee.position) && employee.rating <= 10 &&
      employee.rating > 0 && employee.seniority > 0.0 && employee.seniority < 50.0
  }
}