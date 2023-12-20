package org.pipeline.ga


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
  maxWeights: Int) {

  @inline
  def numCustomers: Int = costsMatrix.length-1

  def objective(customersOrder: Array[Int]): Int = {
    val path = Array[Int](0) ++ customersOrder
    (0 until path.length-1).map(
      index => packageWeights(index+1) - costsMatrix(index)(index+1)
    ).sum
  }
}


object VehicleRouter {
  def apply(numCustomers: Int, packageWeights: Array[Int], maxWeights: Int): VehicleRouter = {
    require(numCustomers > 1, s"Number of customer $numCustomers should be > 1")
    require(
      packageWeights.length == numCustomers,
      s"Num customers $numCustomers should be == num of packages groups"
    )
    val costsMatrix: Array[Array[Int]] = {
      import scala.util.Random
      Array.fill(numCustomers+1)(Array.fill(numCustomers+1)(8 + Random.nextInt(16)))
    }
    new VehicleRouter(costsMatrix, packageWeights, maxWeights)
  }

  def apply(numCustomers: Int, maxWeights: Int): VehicleRouter = {
    import scala.util.Random

    require(numCustomers > 1, s"Number of customer $numCustomers should be > 1")
    val costsMatrix: Array[Array[Int]] =
      Array.fill(numCustomers+1)(Array.fill(numCustomers+1)(8 + Random.nextInt(16)))
    val packageWeights: Array[Int] = Array.fill(numCustomers)(2 + Random.nextInt(6))
    new VehicleRouter(costsMatrix, packageWeights, maxWeights)
  }
}
