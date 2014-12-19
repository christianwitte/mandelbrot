import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.routing.RoundRobinPool
import scala.collection.mutable
import scala.concurrent.duration._
import java.awt.Color

object Main extends App {

  calculate(4, 10)

  sealed trait MandelMsg
  case object Calculate extends MandelMsg
  case class Work(start: Int, numYPixels: Int) extends MandelMsg
  case class Result(elements: mutable.Map[(Int, Int), Color]) extends MandelMsg
  case class MandelResult(elements: mutable.Map[(Int, Int), Color], duration: Duration) extends MandelMsg

  class Master(numWorkers: Int, numSegments: Int, resultHandler: ActorRef) extends Actor with MyConfig {
    var numResults: Int = 0
    val start = System.currentTimeMillis()
    var set = mutable.Map[(Int, Int), Color]()

    val workerRouter = context.actorOf(Props[Worker].withRouter(RoundRobinPool(numWorkers)), name = "workerRouter")

    def receive = {
      case Calculate ⇒ {
        val pixelsPerSegment = canvasHeight / numSegments
        for (i ← 0 until numSegments)
          workerRouter ! Work(i * pixelsPerSegment, pixelsPerSegment)
      }
      case Result(elements) ⇒ {
        set ++= elements
        numResults += 1
        if (numResults == numSegments) {
          val duration = (System.currentTimeMillis() - start).millis
          resultHandler ! MandelResult(set, duration)
          context.stop(self)
        }
      }
    }
  }

  class Worker extends Actor with MyConfig with SmoothCalc {
    def runCalc(calc: ⇒ (Int, Int) ⇒ ((Int, Int), Color), start: Int, numYPixels: Int) = {
      var set = mutable.Map[(Int, Int), Color]()
      for (px ← 0 until canvasWidth) {
        for (py ← 0 until start + numYPixels) {
          set += calc(px, py)
        }
      }
      set
    }

    def receive = {
      case Work(start, numYPixels) ⇒ sender ! Result(runCalc(calculate, start, numYPixels))
    }
  }

  class ResultHandler extends Actor with MyConfig {

    def receive = {
      case MandelResult(set, duration) ⇒ {
        if (false) {
          var outfile = new java.io.FileOutputStream("mandelOut.csv")
          var outStream = new java.io.PrintStream(outfile)
          def transform(line: ((Int, Int), Color)): String = "%s, %s, %s".format(line._1._1, line._1._2, line._2)
          set foreach { line ⇒ outStream.println(transform(line)) }
          outStream.close()
        }
        println("Completed in %s!".format(duration))
        context.system.shutdown()
        new MandelDisplay(set, canvasHeight, canvasWidth);
      }
    }
  }

  trait MyConfig extends MandelConfig {
    def canvasHeight: Int = 700
    def canvasWidth: Int = 1000
    def maxIter: Int = 1000
    def palette: Map[Int, java.awt.Color] = ???
  }

  def calculate(numWorkers: Int, numSegments: Int) {
    val system = ActorSystem("MandelbrotSystem")
    val resultHandler = system.actorOf(Props[ResultHandler], name = "resultHandler")
    val master = system.actorOf(Props(new Master(numWorkers, numSegments, resultHandler)), name = "master")
    master ! Calculate
  }
}
