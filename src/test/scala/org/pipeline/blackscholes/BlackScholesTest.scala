package org.pipeline.blackscholes

import org.pipeline.blackscholes.BlackScholes.{BSParams, monteCarlo}
import org.scalatest.flatspec.AnyFlatSpec

private[blackscholes] final class BlackScholesTest  extends AnyFlatSpec{

  it should "Succeed process Black-Scholes" in {
    val S0 = 80.0
    val r = 0.05
    val sigma = 0.15
    val K = 0.05
    val bsParams = BSParams(S0, r, K, sigma, 100)
    val priceHistory = monteCarlo(bsParams, S0)
    println(priceHistory.mkString("\n"))
  }
}
