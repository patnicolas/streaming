/**
 * Copyright 2022,2023 Patrick R. Nicolas. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 */
package org.streamingeval


package object spark {
  /**
   * Default entry for architecture (Kafka, Spark) parameters
   *
   * @param key       Native name of the parameter
   * @param value     Typed value of the parameter
   * @param isDynamic Is parameter tunable
   * @param paramType Type of parameter (Int, String, Double,....)
   *
   * @author Patrick Nicolas
   * @version 0.0.3
   */
  case class ParameterDefinition(key: String, value: String, isDynamic: Boolean, paramType: String) {
    override def toString: String = s"$key $value ${if (isDynamic) "dynamic" else "static"}, $paramType"
  }


  /**
   * Define tuning parameters
   */
  trait TuningParameters[T <: TuningParameters[T]] {
    def getTunableParams: Seq[ParameterDefinition]
  }
}
