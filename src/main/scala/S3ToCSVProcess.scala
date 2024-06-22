import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
object S3ToCsvProcess {
    def getSparkSession: SparkSession = {
        val spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("KeySpaces-Parquet Spark")
                .config("spark.ui.port", "4081")
                .config("spark.driver.bindAddress", "127.0.0.1")
                .config("spark.driver.port", "7077")
                .config("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider")
                .config("spark.cassandra.connection.host","***********")
                .config("spark.cassandra.connection.port", "9142")
                .config("spark.cassandra.connection.ssl.enabled", "true")
                .config("spark.cassandra.auth.username", "********")
                .config("spark.cassandra.auth.password", "**********")
                .config("spark.cassandra.input.consistency.level", "LOCAL_QUORUM")
                .config("spark.cassandra.connection.ssl.trustStore.path", "/Users/smukka/cassandra_truststore.jks")
                .config("spark.cassandra.connection.ssl.trustStore.password", "*******")
                .getOrCreate()
        val hadoopConfig = spark.sparkContext.hadoopConfiguration
        hadoopConfig.set("fs.s3a.access.key", "********")
        hadoopConfig.set("fs.s3a.secret.key", "**********")
        hadoopConfig.set("fs.s3a.endpoint", "s3.amazonaws.com")
        hadoopConfig.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
        hadoopConfig.set("fs.s3a.path.style.access", "true")
        hadoopConfig.set("fs.s3a.connection.ssl.enabled", "false")
        hadoopConfig.set("fs.s3a.region", "eu-north-1")
        spark
    }  

    def readFromS3ParquetFormat(spark: SparkSession, path: String): DataFrame = {
        val df = spark.read.parquet(path)
        df
    }


    def printDF(df: DataFrame): Unit = {
        df.show
    }

    def main(args: Array[String]): Unit = {
        val spark = getSparkSession
        val input_path = "s3a://scala-akka-bucket/zaragoza_data.csv"
        val output_path = "s3a://scala-akka-bucket/zaragoza_data/"

        val zaragozaDataDf = spark.read
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(input_path)
        

        zaragozaDataDf.write
            .format("org.apache.spark.sql.cassandra")
            .options(Map("table" -> "air_quality_data_new", "keyspace" -> "tutorialkeyspace"))
            .mode("append")
            .save()


        val zaragozaDataCassandraDf = spark.read
                    .format("org.apache.spark.sql.cassandra")
                    .options(Map("table" -> "air_quality_data_new", "keyspace" -> "tutorialkeyspace"))
                    .load()

        
        val zaragozaDataRenamedDf = zaragozaDataCassandraDf.columns.foldLeft(zaragozaDataCassandraDf)
        { 
            (tempDF, colName) =>
                tempDF.withColumnRenamed(colName, colName.replace(" ", "_").replace("(", "").replace(")", "").replace("-", "_"))
        }
        zaragozaDataRenamedDf.write.mode("overwrite").parquet(output_path)
        

        //reading from S3 
        val zaragozaDataParquetDf = readFromS3ParquetFormat(spark, output_path)

        //agg operations on data
        val countOfRecords = zaragozaDataParquetDf.groupBy("station_name").agg(count("*"))
        printDF(countOfRecords)

        val avg_temp_records = zaragozaDataParquetDf.groupBy("station_name").agg(
            avg(col("no2")).alias("avg_02"),
            avg(col("o3")).alias("avg_03"),
            avg(col("pm10")).alias("avg_pm10")
        )
        printDF(avg_temp_records)

        val max_min_temp = zaragozaDataParquetDf.groupBy("station_name").agg(max("temp").alias("max_temp"), min("temp").alias("min_temp"))
        printDF(max_min_temp)
    
        val totalPmPerStation = zaragozaDataParquetDf.groupBy("station_name").agg(sum("PM10").alias("total_PM10"))
        printDF(totalPmPerStation)
       
        
        val Total_Precipitation = zaragozaDataParquetDf.groupBy("station_name").agg(sum("Total_Percipitation").alias("total_prep"))
        printDF(Total_Precipitation)

        val total_soil_temp = zaragozaDataParquetDf.groupBy("station_name").agg(sum("Soil_Temp").alias("total_soil_temp"))
        printDF(total_soil_temp)
        
        val TempPerStation = zaragozaDataParquetDf.filter(col("temp") >= 27)
        printDF(TempPerStation)

        val data_stationName = zaragozaDataParquetDf.filter(col("station_name") === "Roger De Flor")
        printDF(data_stationName)

        val data_stationName_info = zaragozaDataParquetDf.filter(col("station_name") === "Roger De Flor" && col("PM10") < 32.89)
        printDF(data_stationName_info)
        
    }
}