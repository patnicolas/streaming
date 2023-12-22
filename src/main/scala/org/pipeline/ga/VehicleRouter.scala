package org.pipeline.ga

import scala.collection.mutable.ListBuffer


/**
 * The vehicle routing problem is one consisting of a single depot, n customers, and m trucks.
 * For each customer, the vehicle must pick up a certain amount of weight, Wj, where j is the
 * customer number. For problems with time considerations there is a constant stop time, s, at
 * each customer. The objective is to minimize the total distance traveled by all vehicles
 * where dij is the distance from customer i to customer j.
 *
 * @param costsMatrix Matrix of costs to travel between customer i and j
 * @param packageWeights Weight package suggested to limit
 * @author Patrick Nicolas
 */
private[ga] final class VehicleRouter private (
  costsMatrix: Array[Array[Int]],
  packageWeights: Array[Int],
  override val mutationProbThreshold: Double) extends MutationOp {

  @inline
  def numCustomers: Int = costsMatrix.length-1

  def objective(customersOrder: Array[Int]): Int = {
    require(
      customersOrder.length +1 == costsMatrix.length,
      s"Number of customers ${customersOrder.length} should eq size cost matrix ${costsMatrix.length}"
    )

    val path = Array[Int](0) ++ customersOrder
    (0 until path.length-1).map(
      index => packageWeights(index+1) - costsMatrix(index)(index+1)
    ).sum
  }

  override def toString: String = {
    val costsMatrixStr = costsMatrix.map(_.mkString(" ")).mkString("\n")
    val packageWeightsStr = packageWeights.mkString(" ")
    s"Cost matrix:\n${costsMatrixStr}\nPackages Weights: $packageWeightsStr\nMutation prob " +
      s"threshold: $mutationProbThreshold"
  }

}


private[ga] object VehicleRouter {
  def apply(packageWeights: Array[Int], mutationProbThreshold: Double): VehicleRouter = {
    require(packageWeights.length > 0, s"Number of packages ${packageWeights.length} should be > 1")

    val numCustomers = packageWeights.length
    val costsMatrix: Array[Array[Int]] = {
      import scala.util.Random
      Array.fill(numCustomers+1)(Array.fill(numCustomers+1)(8 + Random.nextInt(16)))
    }
    new VehicleRouter(costsMatrix, packageWeights, mutationProbThreshold)
  }

  def apply(numCustomers: Int, mutationProbThreshold: Int): VehicleRouter = {
    import scala.util.Random
    require(numCustomers > 1, s"Number of customer $numCustomers should be > 1")

    val costsMatrix: Array[Array[Int]] =
      Array.fill(numCustomers+1)(Array.fill(numCustomers+1)(8 + Random.nextInt(16)))
    val packageWeights: Array[Int] = Array.fill(numCustomers)(2 + Random.nextInt(6))
    new VehicleRouter(costsMatrix, packageWeights, mutationProbThreshold)
  }

  /**
   *
   * @param numCustomers
   * @return
   */

  def generateRoutes(numCustomers: Int): Map[Int, String] = {
    def swap(input: Array[Int], fromIndex: Int, toIndex: Int): Unit = {
      val temp = input(fromIndex)
      input(fromIndex) = input(toIndex)
      input(toIndex) = temp
    }

    def permute(customerSet: Array[Int], cursor: Int, collector: ListBuffer[String]): Unit = {
      if(cursor == customerSet.length -1) {
        println(customerSet.mkString(" "))
        collector.append(customerSet.mkString(" "))
      }

      (cursor until customerSet.length ).foreach(
        index => {
          swap(customerSet, cursor, index)
          permute(customerSet, cursor + 1, collector)
          swap(customerSet, cursor, index)
        }
      )
    }
    val collector = ListBuffer[String]()
    permute(Array.tabulate(numCustomers)(n => n+1), 0, collector)
    collector.zipWithIndex.map{ case (seq, index) => (index, seq)}.toMap
  }
}
