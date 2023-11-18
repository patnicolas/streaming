spark-submit --driver-memory 40g --class org.streaming.spark.weatherTracking.WeatherTrackingApp \
--master \
local[16] \
target/weatherTracking-0.0.5-jar-with-dependencies.jar $1 $2