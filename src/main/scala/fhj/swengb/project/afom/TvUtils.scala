package fhj.swengb.project.afom

import java.io.File
import javafx.scene.control.{TreeCell, TreeView}
import javafx.util.Callback

/**
  * Created by Steve on 22.01.2016.
  */
object TvUtils {

  def mkTreeCellFactory[T](f: TreeView[T] => TreeCell[T]): Callback[TreeView[T], TreeCell[T]] = {
    new Callback[TreeView[T], TreeCell[T]] {
      override def call(param: TreeView[T]): TreeCell[T] = f(param)
    }
  }

  def fileToString(f: File): String ={
    f.getName
  }


  def mkNewCell[T](typeToString: T => String)(lv: TreeView[T]): TreeCellImpl[T] = {

    class newCell extends TreeCellImpl[T] {
      override def updateItem(t: T, empty: Boolean): Unit = {
        super.updateItem(t, empty)
        if (t != null) {
          setText(typeToString(t))
        }
        else setText(null)
      }
    }
    new newCell()
  }

}
