spark-submit --driver-memory 40g --class org.batcheval.Cleanser --master local[16]
target/Examples-0.1-jar-with-dependencies.jar $1 $2
