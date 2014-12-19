import javax.swing.JFrame
import java.awt.{Graphics, Color, Dimension}
import scala.collection.mutable

class MandelDisplay(points: mutable.Map[(Int, Int), Color], height: Int,
  width: Int) extends JFrame  {
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  setPreferredSize(new Dimension(height, width))
  pack
  setResizable(true)
  setVisible(true)
  override def paint(g: Graphics) {
    super.paint(g)
    
    for(px <- 0 until width) {
      for (py <- 0 until height) {
        val colorVal = points(px,py)

        g.setColor(colorVal)
        g.drawLine(px, py, px, py)
      }
    }
  }
}
