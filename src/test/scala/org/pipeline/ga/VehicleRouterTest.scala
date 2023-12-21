package org.pipeline.ga

import org.scalatest.flatspec.AnyFlatSpec

private[ga] final class VehicleRouterTest extends AnyFlatSpec {

  it should "Succeed instantiating Vehicle router" in {
    val vehicleRouter = VehicleRouter(Array[Int](39, 12, 21))
    println(vehicleRouter.toString)
  }
}
