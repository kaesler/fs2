package fs2
package benchmark

import QuickProfile._

object FastBranchVsOld extends App {

  val N = 10000
  // use random initial value to prevent loop getting optimized away
  def init = (math.random * 10).toInt

  def segmentAppendTest(n: Int) =
    timeit(s"segment append ($n)") {
      import fs2.fast._
      (0 until n).foldLeft(Segment.singleton(0))((acc,i) => acc ++ Segment.singleton(i)).fold(0)(_ + _).run
    }

  def segmentPushTest(n: Int) =
    timeit(s"segment push ($n)") {
      import fs2.fast._
      (0 until n).foldRight(Segment.singleton(0))((i,acc) => acc.push(Chunk.singleton(i))).fold(0)(_ + _).run
    }

  println("--- smart chunk vs dumb chunk --- ")
  suite(
    timeit("baseline map (2)") {
      val bs = new Array[Int](1000)
      var i = 0
      while (i < bs.length) {
        bs(i) += 1
        i += 1
      }
      var j = 0
      while (j < bs.length) {
        bs(j) += 1
        j += 1
      }
      bs(0).toLong
    },
    timeit("dumb chunk map (2)") {
      import fs2.fast._
      Segment.array(new Array[Int](1000))
             .map(_ + 1)
             .map(_ + 1)
             .run(null).hashCode.toLong
    },
    timeit("smart chunk map (2)") {
      Chunk.bytes(new Array[Byte](1000))
           .map(_ + (1:Byte))
           .map(_ + (1:Byte))
           .size.toLong
    },
    timeit("smart chunk map (1)") {
      var x : Long = (math.random * 10).toLong
      Chunk.bytes(new Array[Byte](50))
           .map(_ + (1:Byte))
           .map(b => x += b)
      x
    },
    timeit("dumb chunk map (1)") {
      import fs2.fast._
      var x : Long = (math.random * 10).toLong
      Chunk.bytes(new Array[Byte](50))
           .map(_ + (1:Byte))
           .map(b => x += b)
           .map(_ => ()).run
      x
    }
  )
  //suite(
  //  segmentPushTest(100),
  //  segmentPushTest(200),
  //  segmentPushTest(400),
  //  segmentPushTest(800),
  //  segmentPushTest(1600),
  //  segmentPushTest(3200),
  //  segmentAppendTest(100),
  //  segmentAppendTest(200),
  //  segmentAppendTest(400),
  //  segmentAppendTest(800),
  //  segmentAppendTest(1600),
  //  segmentAppendTest(3200)
  //  //timeit("segment new") {
  //  //  import fs2.fast._
  //  //  Segment.from(init).take(N.toLong).sum(0L).run
  //  //},
  //  //timeit("new fs2") {
  //  //  import fs2.fast._
  //  //  def sum[F[_]](acc: Int, s: Stream[F,Int]): Pull[F,Int,Unit] =
  //  //    s.unsegment flatMap {
  //  //      case None => Pull.output1(acc)
  //  //      case Some((hd,s)) => sum(hd.fold(acc)(_ + _).run, s)
  //  //    }
  //  //  sum(init, Stream.range(0, N)).close.toVector.head
  //  //},
  //  //timeit("old fs2") {
  //  //  Stream.range(0, N).fold(init)(_ + _).toList.head.toLong
  //  //},
  //  //{ val nums = List.range(0, N)
  //  //  timeit("boxed loop") { nums.foldLeft(init)(_ + _) }
  //  //},
  //  //timeit("while loop") {
  //  //  var sum = init
  //  //  var i = 0
  //  //  while (i < N) { sum += i; i += 1 }
  //  //  sum
  //  //}
  //)
  println("---")
}