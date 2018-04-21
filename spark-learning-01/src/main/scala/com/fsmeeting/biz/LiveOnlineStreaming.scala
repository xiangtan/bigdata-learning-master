package com.fsmeeting.biz

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.{JSON, JSONObject}
import com.fsmeeting.RedisClient
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object LiveOnlineStreaming {

  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName)
    val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(10)) // 10s per batch
    val topicMap: Map[String, Int] = Map("live-online-users" -> 1)
    val kafkaStream: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(ssc, "node-1:2181",
      "online-streaming-group", topicMap)
    val jsonData: DStream[JSONObject] = kafkaStream.flatMap(line => {
      Some(JSON.parseObject(line._2))
    })

    //{"bpartnerId":355,"num":57,"roomId":1318,"time":1524236144977}
    var t: DStream[(String, Int)] = jsonData.map(x => {
      val sep: String = ":";
      val bpartnerId = x.getString("bpartnerId")
      val roomId = x.getString("roomId")
      val time = x.getLongValue("time")
      val timeMin: String = new SimpleDateFormat("yyyyMMddHHmm").format(new Date(time))
      val num = x.getIntValue("num")

      (bpartnerId + sep + roomId + sep + timeMin, num)
    })

    val t1: DStream[(String, Iterable[Int])] = t.groupByKey()

    val t2: DStream[(String, Int)] = t1.mapValues(_.max)

    t2.foreachRDD(rdd => {
      rdd.foreachPartition(partitionOfRecords => {
        partitionOfRecords.foreach(pair => {
          val jedis = RedisClient.pool.getResource
          jedis.select(1)
          val pre = jedis.get(pair._1)
          val cur = pair._2
          if (pre == null || cur > pre.toInt) {
            jedis.set(pair._1, pair._2.toString)
          }
          RedisClient.pool.returnResource(jedis)
        })
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}