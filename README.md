### streaming  0.0.2 Patrick Nicolas Last update 06.03.2023.    
2023


![Evaluation and tips for Kafka and Spark streaming](images/streaming.jpg)


Evaluation of Apache Kafka and Spark streaming functionality in Scala

## Reference
- [Open Source Lambda Architecture for Deep Learning](http://patricknicolas.blogspot.com/2021/06/open-source-lambda-architecture-for.html)


## Environment
| Package      | Version |
|:-------------|:--------|
| Scala        | 2.13.11 |
| Apache Spark | 3.3.2   |
| Apache Kafka | 3.4.0   |
| Jackson ser  | 2.13.1  |
| Kubernetes   | 16.0.0  |



## Updates
| Date       | Version |
|:-----------|:--------|
| 01.20.2023 | 0.0.1   |
| 06.03.2023 | 0.0.2   |


## Packages
|Package|Description|
|:--|:--|
|util|Utilities classes|
|kafka|Classes related to Kafka producer/consumer, service management and streaming|
|spark|Classes related to spark datasets and streaming     


## Deployment
From dockerfile for local deployment for a root directory 'myhome':    
Linux X64: **docker --build-args jdk_target=linux_x64 home=myhome build -t streaming**         
MacOS X64: **docker --build-args jdk_target=macos_x64 home=myhome build -t streaming**
MacOS ARM: **docker --build-args jdk_target=macos_aarch64 home=myhome build -t streaming**

