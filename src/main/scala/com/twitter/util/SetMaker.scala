package com.twitter.util

import scala.collection.mutable.{Map, Set}
import scala.collection.jcl.MapWrapper
import java.util.concurrent.TimeUnit
import com.google.common.collect.{MapMaker => GoogleMapMaker}
import com.google.common.base.Function

object SetMaker {
  def apply[A](f: Config[A] => Any): Set[A] = {
    val config = new Config[A]
    f(config)
    config()
  }

  class Config[A] {
    private val mapMaker = new GoogleMapMaker[A, A]

    def weakValues = { mapMaker.weakKeys; mapMaker.weakValues; this }
    def softValues = { mapMaker.softKeys; mapMaker.softValues; this }
    def concurrencyLevel(level: Int) = { mapMaker.concurrencyLevel(level); this }
    def initialCapacity(capacity: Int) = { mapMaker.initialCapacity(capacity); this }
    def expiration(expiration: Duration) = { mapMaker.expiration(expiration.inMillis, TimeUnit.MILLISECONDS); this }

    def apply() = {
      val javaMap: java.util.Map[A, A] = mapMaker.makeMap()
      MapToSet(new MapWrapper[A, A] {
        val underlying = javaMap
      })
    }
  }
}

object MapToSet {
  def apply[A](map: Map[A, A]): Set[A] = new MapToSetAdapter(map)
}

class MapToSetAdapter[A](map: Map[A,A]) extends Set[A] {
  def +=(elem: A) = map(elem) = elem
  def -=(elem: A) = map -= elem
  def size = map.size
  def elements = map.keys
  def contains(elem: A) = map.contains(elem)
}