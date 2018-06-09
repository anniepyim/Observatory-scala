package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.power

import math.{Pi, atan, pow, sinh, toDegrees}

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 {

  val power: Int = 5
  val width: Int = pow(2, power).toInt
  val height: Int = pow(2, power).toInt
  val alpha: Int = 256

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    val x = point.x
    val y = point.y
    val x_1 = 1 - point.x
    val y_1 = 1 - point.y
    d00 * x_1 * y_1 + d10 * x * y_1 + d01 * x_1 * y + d11 * x * y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {
    val offX = (tile.x * pow(2, power)).toInt
    val offY = (tile.y * pow(2, power)).toInt
    val offZ = tile.zoom
    val coords = for {
      i <- 0 until height
      j <- 0 until width
    } yield (i, j)

    val pixels = coords.par
      .map({case (y, x) => Tile(x + offX, y + offY, power + offZ)})
      .map(Interaction.tileLocation)
      .map(interpolate(grid, _))
      .map(Visualization.interpolateColor(colors, _))
      .map(col => Pixel(col.red, col.green, col.blue, alpha))
      .toArray

    Image(width, height, pixels)
  }

  def interpolate(grid: GridLocation => Temperature,
                  loc: Location): Temperature = {
    val lat = loc.lat.toInt
    val lon = loc.lon.toInt
    val pt00 = GridLocation(lat, lon)
    val pt01 = GridLocation(lat + 1, lon)
    val pt10 = GridLocation(lat, lon + 1)
    val pt11 = GridLocation(lat + 1, lon + 1)
    val point = CellPoint(loc.lon - lon, loc.lat - lat)
    bilinearInterpolation(point, grid(pt00), grid(pt01), grid(pt10), grid(pt11))
  }

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateGrids[Data](
                           yearlyData: Iterable[(Year, Data)],
                           generateImage: (Year, Tile, Data) => Unit
                         ): Unit = {
    val tiles = for {
      zoom <- 0 until 1
      x <- 0 until pow(2, zoom).toInt
      y <- 0 until pow(2, zoom).toInt
      yearData <- yearlyData
    } yield generateImage(yearData._1, Tile(x, y, zoom), yearData._2)
  }

}
