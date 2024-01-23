/**
 * Copyright 2022,2024 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.pipeline.blackscholes

import java.lang.Math.exp
import scala.annotation.tailrec
import scala.util.Random


/**
 * Singleton for the computation and memoization of the Black-Scholes equation for
 * option pricing
 * @see https://patricknicolas.blogspot.com/2013/07/a-fast-black-scholes-simulator.html
 * @author Patrick Nicolas
 */
object BlackScholes {

  /**
   * Wrapper for the parameters of the Black-Scholes equation
   * @param S  Price of underlying security
   * @param K  Strike price
   * @param r  Risk free interest rate
   * @param sigma Standard deviation for the Gaussian distribution representing volatility
   * @param T Expiration time of the option
   * @author Patrick Nicolas
   */
  case class BSParams(S: Double, K: Double, r: Double, sigma: Double, T: Double)


  /**
   * Component functions for the computation of the Black-Scholes formula.
   * @author Patrick Nicolas
   */
  object BSParams{
    val fLog = (p: BSParams) => Math.log(p.r / p.T)
    val fMul = (p: BSParams) => p.r * p.T
    val fPoly = (p: BSParams) => 0.5 * p.sigma * p.sigma * p.T
    val fExp = (p: BSParams) => -p.K * Math.exp(-p.r * p.T)
  }


  /**
   * Caching of result of intermediate computation of the Black-Scholes formula
   * @param bs1 Cache value 1
   * @param bs2 Cache value 2
   * @param bs3 Cache value 3
   * @param bs4 Cache value 4
   * @param bs5 Cache value 5
   * @author Patrick Nicolas
   */
  case class PartialResult(bs1: Double, bs2: Double, bs3: Double, bs4: Double, bs5: Double){
    import BSParams._

    lazy val d1: Double = (bs1 + bs2 + bs3) / bs4
    def f1(p: BSParams): PartialResult = this.copy(bs2 = fLog(p))
    def f2(p: BSParams): PartialResult = this.copy(bs2 = fMul(p), bs5 = fExp(p))
    def f3(p: BSParams): PartialResult = this.copy(bs3 = fPoly(p), bs5 = fExp(p))
  }

  /**
   * State of the computation of the Black-Scholes formula
   * @param p Black-Scholes parameters
   * @param pr Partial result (cached) of intermediate computation of BS
   */
  class State(p: BSParams, pr: PartialResult){
    def setS(newS: Double): State = new State(p.copy(S = newS), pr)

    def setR(newR: Double): State = new State(p.copy(r = newR), pr)

    def setSigma(newSigma: Double): State = new State(p.copy(sigma = newSigma), pr)

    final def getBSParams: BSParams = p

    /**
     * Computation of a Call option with reusable intermediate results
     */
    lazy val call: Double = {
      import org.apache.commons.math3.analysis.function.Gaussian

      val gauss = new Gaussian
      p.S * gauss.value(pr.d1)-pr.bs5 * gauss.value(pr.d1 - pr.bs4)
    }
  }

  /**
   * Monte Carlo simulation that estimate the future price of a security. This implementation
   * relies on fast tail recursion
   *
   * @param p Parameters for the Black-Scholes formula
   * @param s0 Initial price of the underlying security
   * @return Predicted, simulated price of the underlying security
   */
  def monteCarlo(p: BSParams, s0: Double): List[Double] = {
    val initialValue = s0 + exp((p.r-0.5*p.sigma*p.sigma)+p.sigma*Random.nextGaussian)
    monteCarlo(p, 0, List[Double](initialValue))
  }

  @tailrec
  private def monteCarlo(p: BSParams, t: Int, values: List[Double]): List[Double] = {
    import Math._
    if(t >= p.T)
      values.reverse
    else {
      val st = values.last * exp((p.r-0.5*p.sigma*p.sigma)+p.sigma*Random.nextGaussian)
      monteCarlo(p, t+1, st :: values)
    }
  }
}
