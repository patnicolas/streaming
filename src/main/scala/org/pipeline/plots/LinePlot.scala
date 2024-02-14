/**
 * Copyright (c) 2020-2024  Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.pipeline.plots

import org.jfree.chart.plot.{CategoryPlot, PlotOrientation}
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.pipeline.plots.Plot.{validateDisplay, validateDisplayUtils}

import java.awt.{BasicStroke, Color, Shape, Stroke}
import org.jfree.chart.ChartFactory
import org.jfree.chart.util.ShapeUtils
import org.jfree.data.category.{CategoryDataset, DefaultCategoryDataset}
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}

/**
 * Class to create a Line plot using the JFreeChart library.
 *
 * @constructor Create a Line plot instance
 * @throws IllegalArgumentException if the class parameters are undefined
 * @param legend  legend for the plot of type '''PlotInfo'''
 * @param theme  Configuration for the display of plots of type '''PlotTheme'''
 * @author Patrick Nicolas
 * @see Scala for Machine Learning''Appendix'' Visualization
 * @see http://www.jfree.org
 */
class LinePlot(legend: Legend, theme: PlotTheme) extends Plot(legend, theme) {
  val strokeList: List[Stroke] = LinePlot.strokeList

  private val colors: Array[Color] = Array[Color](
    Color.black, Color.red, Color.darkGray, Color.blue
  )
  private val shapes: Array[Shape] = Array[Shape](
    ShapeUtils.createDiamond(1.0F),
    ShapeUtils.createRegularCross(1.0F, 1.0F),
    ShapeUtils.createDownTriangle(1.0F)
  )

  /**
   * DisplayUtils array of tuple (x,y) in a Line plot for a given width and height
   * @param xy Array of pair (x,y)
   * @param w Width for the display (pixels)
   * @param h Height of the chart (pixels)
   * @return true if the plot is displayed, false otherwise
   * @throws IllegalArgumentException if the dataset is undefined or the width or height are
   * out of bounds.
   */
  override def display(xy: Vector[(Double, Double)], w: Int, h: Int): Boolean = {
    val validDisplay = validateDisplay[Vector[(Double, Double)]](xy, w, h, "LinePlot.display")
    if (validDisplay) {
      val catDataset = new DefaultCategoryDataset
      xy.foreach(x => catDataset.addValue(x._1, legend.xLabel, String.valueOf(x._2.toInt)))
      draw(catDataset, w, h)
    }
    validDisplay
  }

  /**
   * DisplayUtils a vector of Double value in a Line plot with counts [0, n] on X-Axis and
   * vector value on Y-Axis with a given width and height
   * @param y Array of values
   * @param w Width for the display (pixels)
   * @param h Height of the chart (pixels)
   * @return true if the plot is displayed, false otherwise
   * @throws IllegalArgumentException if the dataset is undefined or the width or height are
   * out of bounds.
   */
  override def display(y: Array[Double], w: Int, h: Int): Boolean = {
    val validDisplay = validateDisplayUtils(y, w, h, "LinePlot.display")
    if (validDisplay) {
      val catDataset = new DefaultCategoryDataset
      y.zipWithIndex.foreach {
        case (x, n) =>
          catDataset.addValue(x, legend.xLabel, n)
      }
      draw(catDataset, w, h)
    }
    validDisplay
  }

  import scala.collection._
  def display(
    xys: immutable.List[(Vector[Double], String)],
    w: Int,
    h: Int
  ): Boolean = {
    val validDisplay = validateDisplay[
      List[(Vector[Double], String)]](xys, w, h, "LinePlot.display")
    if (validDisplay) {

      val seriesCollection = new XYSeriesCollection
      xys.foreach {
        case (x, s) =>
          val xSeries = new XYSeries(s)
          x.zipWithIndex.foreach { case (z, n) => xSeries.add(n.toDouble, z) }

          seriesCollection.addSeries(xSeries)
      }

      val chart = ChartFactory.createXYLineChart(null, legend.xLabel, legend.yLabel,
        seriesCollection,
        PlotOrientation.VERTICAL, true, true, false)
      setTitle(legend.title, chart)

      val plot = chart.getXYPlot
      plot.setBackgroundPaint(theme.paint(w, h))
      plot.setDomainGridlinePaint(Color.darkGray)
      plot.setRangeGridlinePaint(Color.lightGray)

      val xyLineRenderer: XYLineAndShapeRenderer =
        plot.getRenderer.asInstanceOf[XYLineAndShapeRenderer]

      xys.indices foreach (n => {
        xyLineRenderer.setSeriesPaint(n, colors(n % colors.length))
        xyLineRenderer.setSeriesShapesVisible(n, true)
        xyLineRenderer.setSeriesShape(n, shapes(n % shapes.length))
      })

      createFrame(s"${legend.title}", chart)
    }
    validDisplay
  }

  private def draw(catDataset: CategoryDataset, w: Int, h: Int): Unit = {
    val chart = ChartFactory.createLineChart(legend.title, legend.xLabel, legend.yLabel, catDataset,
      PlotOrientation.VERTICAL, false, false, false)

    setTitle(legend.title, chart)
    val plot = chart.getPlot.asInstanceOf[CategoryPlot]

    val renderer = new LineAndShapeRenderer {
      override def getItemStroke(row: Int, col: Int): Stroke = strokeList.head
    }

    plot.setRenderer(renderer)
    renderer.setSeriesShape(0, ShapeUtils.createDiamond(1.0F))
    renderer.setSeriesPaint(0, theme.color(0))

    plot.setBackgroundPaint(theme.paint(w, h))
    createFrame(legend.title, chart)
  }
}

/**
 * Singleton for Line plot using the JFreeChart library
 * @author Patrick Nicolas
 * @since  0.97 November 18, 2013
 * @version 0.97
 * @see Scala for Machine Learning "Appendix" Visualization
 * @see http://www.jfree.org
 */
object LinePlot {
  import BasicStroke._
  import scala.collection._

  private val DEFAULT_WIDTH = 320
  private val DEFAULT_HEIGHT = 260

  val strokeList: List[Stroke] = immutable.List[Stroke](
    new BasicStroke(1.0f, CAP_BUTT, JOIN_BEVEL, 2.0f, null /*Array[Float](2.0f, 2.0f) */ , 0.0f),
    new BasicStroke(1.0f, CAP_BUTT, JOIN_BEVEL, 2.0f, Array[Float](1.0f, 1.0f), 0.0f),
    new BasicStroke(1.0f, CAP_ROUND, JOIN_ROUND, 2.0f, Array[Float](1.0f, 1.0f), 0.0f)
  )

  def display(y: Vector[Double], legend: Legend, theme: PlotTheme): Boolean =
    createPlot(DisplayUtils.isChart, legend, theme).display(y.toArray, DEFAULT_WIDTH, DEFAULT_WIDTH)

  def display(y: Array[Double], legend: Legend, theme: PlotTheme): Boolean =
    createPlot(DisplayUtils.isChart, legend, theme).display(y, DEFAULT_WIDTH, DEFAULT_WIDTH)

  def display(xys: List[(Vector[Double], String)], legend: Legend, theme: PlotTheme): Boolean =
    createPlot(DisplayUtils.isChart, legend, theme).display(xys, DEFAULT_WIDTH, DEFAULT_WIDTH)

  private def createPlot(isDefined: Boolean, legend: Legend, theme: PlotTheme): LinePlot = {
    require(isDefined, s"${legend.key} display Cannot plot an undefined time series")

    new LinePlot(legend, theme)
  }

}
// ------------------------  EOF ----------------------------------------------