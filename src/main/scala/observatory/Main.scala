package observatory

import java.io.File
import java.nio.file.{Paths, Files}

import org.apache.log4j.{Level, Logger}

object Main extends App {
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)

  val temps = Extraction.locateTemperatures(1997, "/stations.csv", "/1997.csv")
  val tempsAvg = Extraction.locationYearlyAverageRecords(temps)

//  val basetemps = Extraction.locateTemperatures(1995, "/stations.csv", "/1995.csv")
//  val basetempsAvg = Extraction.locationYearlyAverageRecords(basetemps)

  val tempToCol = List[(Temperature, Color)]((60, Color(255, 255, 255)), (32, Color(255, 0, 0)),
    (12, Color(255, 255, 0)), (0, Color(0, 255, 255)), (-15, Color(0, 0, 255)), (-27, Color(255, 0, 255)),
    (-50, Color(33, 0, 107)), (-60, Color(0, 0, 0)))
//
  val devToCol = List[(Temperature, Color)]((7, Color(0, 0, 0)), (4, Color(255, 0, 0)),
    (2, Color(255, 255, 0)), (0, Color(255, 255, 255)), (-2, Color(0, 255, 255)), (-7, Color(0, 0, 255)))

//  val img = Interaction.tiledev(tempsAvg, basetempsAvg, devToCol, Tile(0, 0, 0))
//  img.output(new File("/Users/ayim/IdeaProjects/observatory/target/test.png"))

  val outdir = "/Users/ayim/IdeaProjects/observatory/target/temperatures"

  def generateAndSaveTile(year: Year, tile: Tile, data: Iterable[(Location, Temperature)]): Unit = {
    val zoom = tile.zoom
    val x = tile.x
    val y = tile.y
    val zoomdir = f"$outdir%s/$year%d/$zoom%d"
    val fn = f"$zoomdir%s/$x%d-$y%d.png"
    Files.createDirectories(Paths.get(zoomdir))

    val img = Interaction.tile(data, tempToCol, tile)
    img.output(new File(fn))
  }

  val data = List[(Year, Iterable[(Location, Temperature)])]((1997, tempsAvg))
  Interaction.generateTiles[Iterable[(Location, Temperature)]](data, generateAndSaveTile)

/** For Deviations **/

//val outdirGrid = "/Users/ayim/IdeaProjects/observatory/target/deviations"
//
//def generateAndSaveTileDev(year: Year, tile: Tile, data: Iterable[(Location, Temperature)], basedata: Iterable[(Location, Temperature)]): Unit = {
//  val zoom = tile.zoom
//  val x = tile.x
//  val y = tile.y
//  val zoomdir = f"$outdirGrid%s/$year%d/$zoom%d"
//  val fn = f"$zoomdir%s/$x%d-$y%d.png"
//  Files.createDirectories(Paths.get(zoomdir))
//
//  val img = Interaction.tiledev(data, basedata, devToCol, tile)
//  img.output(new File(fn))
//}

  //val data = List[(Year, Iterable[(Location, Temperature)], Iterable[(Location, Temperature)])]((2015, tempsAvg, basetempsAvg))
  //Interaction.generateTilesDev[Iterable[(Location, Temperature)]](data, generateAndSaveTileDev)
}
