package observatory

import java.time.LocalDate
import java.nio.file.Paths

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
  * 1st milestone: data extraction
  */
object Extraction {

  val spark: SparkSession =
    SparkSession
      .builder()
      .appName("Observatory")
      .config("spark.master", "local")
      .getOrCreate()

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {


    val stationsRaw = spark.sparkContext.textFile(Paths.get(getClass.getResource(stationsFile).toURI).toString)
    val temperaturesRaw = spark.sparkContext.textFile(Paths.get(getClass.getResource(temperaturesFile).toURI).toString)

    println(year)
    locateTemperaturesSpark(year, stationsRaw, temperaturesRaw).collect().toSeq
  }


//  def locateTemperaturesSpark(year: Year, stationsFile: String,
//                              temperaturesFile: String): RDD[(LocalDate, Location, Temperature)] = {
//
//    val stationsRaw = spark.sparkContext.textFile(stationsFile)
//    val temperaturesRaw = spark.sparkContext.textFile(temperaturesFile)
//
//    locateTemperaturesSpark(year, stationsRaw, temperaturesRaw)
//  }

  def locateTemperaturesSpark(year: Year, stationsRaw: RDD[String],
                              temperaturesRaw: RDD[String]): RDD[(LocalDate, Location, Temperature)] = {

    val stations = stationsRaw
      .map(_.split(','))
      .filter(_.length == 4)
      .map(a => ((a(0), a(1)), Location(a(2).toDouble, a(3).toDouble)))

    val temperatures = temperaturesRaw
      .map(_.split(','))
      .filter(_.length == 5)
      .map(a => ((a(0), a(1)), (LocalDate.of(year, a(2).toInt, a(3).toInt), fahrenheitToCelsius(a(4).toDouble))))

    stations.join(temperatures).mapValues(v => (v._2._1, v._1, v._2._2)).values
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    locationYearlyAverageRecordsSpark(spark.sparkContext.parallelize(records.toSeq)).collect().toSeq
  }

  def locationYearlyAverageRecordsSpark(records: RDD[(LocalDate, Location, Temperature)]): RDD[(Location, Temperature)] = {
    records
      .groupBy(_._2)
      .mapValues(entries => entries.map(entry => (entry._3, 1)))
      .mapValues(_.reduce((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2)))
      .mapValues({case (temp, cnt) => temp / cnt})
  }

  def fahrenheitToCelsius(fahrenheit: Temperature): Temperature =
    (fahrenheit - 32) / 1.8
}
