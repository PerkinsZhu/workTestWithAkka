package com.perkins.stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._

/**
  * Created by PerkinsZhu on 2019/1/7 19:13
  **/
object StreamTest {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()


  def main(args: Array[String]): Unit = {
    testTwo
    system.terminate()
  }

  private def testOne = {

    val source: Source[Int, NotUsed] = Source(1 to 100)
    source.runForeach(println(_))(materializer)
    system.terminate()
  }

  def testTwo(): Unit = {

    final case class Author(handle: String)

    final case class Hashtag(name: String)

    final case class Tweet(author: Author, timestamp: Long, body: String) {
      def hashtags: Set[Hashtag] = body.split(" ").collect {
        case t if t.startsWith("#") ⇒ Hashtag(t.replaceAll("[^#\\w]", ""))
      }.toSet
    }

    val akkaTag = Hashtag("#akka")

    val tweets: Source[Tweet, NotUsed] = Source(
      Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
        Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
        Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
        Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
        Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
        Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
        Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
        Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
        Nil)
    tweets
      .map(_.hashtags) // Get all sets of hashtags ...
      .reduce(_ ++ _) // ... and reduce them to a single set, removing duplicates across all tweets
      .mapConcat(identity) // Flatten the stream of tweets to a stream of hashtags
      .map(_.name.toUpperCase) // Convert all hashtags to upper case
      .runWith(Sink.foreach(println)) // Attach the Flow to a Sink that will finally print the hashtags
  }


}
