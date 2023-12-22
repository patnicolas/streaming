package org.pipeline.ga

import org.pipeline.ga.VehicleRouter.generateRoutes
import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class VehicleRouterTest extends AnyFlatSpec {

  ignore should "Succeed instantiating Vehicle router" in {
    val vehicleRouter = VehicleRouter(Array[Int](39, 12, 21), 0.4)
    println(vehicleRouter.toString)
  }


  it should "Succeed generate all sequence of customers" in {
    val routingMap = generateRoutes(4)
    println(routingMap.map{ case (index, seq) => s"$index: ${seq}"}.mkString("\n"))
  }
}
