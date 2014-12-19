import scala.math._
import java.awt.{ Graphics, Color, Dimension }
 
trait MandelConfig {
  type Result = ((Int, Int), Color)
  def canvasWidth: Int
  def canvasHeight: Int
  def maxIter: Int
  def palette: Map[Int, Color]
}
 
trait MandelCalc {
  def calculate(px: Int, py: Int): ((Int, Int), Color)
}
 
trait EscapeCalc extends MandelCalc {
  self: MandelConfig ⇒
 
  override def calculate(px: Int, py: Int): Result = {
    val x0 = -2.5 + 3.5 * (px.toDouble / canvasWidth.toDouble)
    val y0 = -1 + 2 * (py.toDouble / canvasHeight.toDouble)
 
    var x = 0.0
    var y = 0.0
    var iter = 0
 
    while (x * x + y * y < 4 && iter < maxIter) {
      x = x * x - y * y + x0
      y = 2 * x * y + y0
      iter += 1
    }
 
    ((px, py), palette(iter))
  }
}
 
trait SmoothCalc extends MandelCalc {
  self: MandelConfig ⇒
 
  override def calculate(px: Int, py: Int): Result = {
    
    val (xmin, xmax, ymin, ymax) = (-2.5, 1.0, -1.0, 1.0)
    //0.16125, 0.638438
    //val (xmin, xmax, ymin, ymax) = (.1, .3, .55, .7)
    
    val x0 = xmin + (xmax - xmin) * (px.toDouble / canvasWidth.toDouble)
    val y0 = ymin + (ymax - ymin) * (py.toDouble / canvasHeight.toDouble)
 
    var x = 0.0
    var y = 0.0
    var iter = 0
    var color = maxIter.toDouble
 
    while (x * x + y * y < 65536 && iter < maxIter) {
      var xtemp = x * x - y * y + x0
      y = 2 * x * y + y0
      x = xtemp
      iter += 1
    }
    if (iter < maxIter) {
      var zn = sqrt(x * x + y * y)
      var nu = log(log(zn) / log(2)) / log(2)
      color = (iter.toDouble + 1.0 - nu)
      var ct = color * 10
      var ctt = lerp(floor(ct).toInt, (floor(ct)+1).toInt, ct-floor(ct))
      color = color / 10
    }
    var color1 = Color.getHSBColor((color.toFloat+0.1f)/2.5f, 1.0f, (color * color).toFloat)
 
    ((px, py), color1)
  }

  def lerp(i0: Int, i1: Int, t: Double) = {
    (i0 * t) + (i1 * (1-t))
  }
 
  def lerpcolor(c0: Color, c1: Color, t: Double): Color = {
    val (c0r, c0g, c0b) = (c0.getRed, c0.getGreen, c0.getBlue)
    val (c1r, c1g, c1b) = (c1.getRed, c1.getGreen, c1.getBlue)
    val ti = 1.0 - t
    new Color((c0r * t).toInt + (c1r * ti).toInt, (c0g * t).toInt + (c1g * ti).toInt, (c0b * t).toInt + (c1b * ti).toInt)
  }
 
}
