package fhj.swengb.project.afom

import javafx.beans.value.ObservableValue
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.{TableColumn, TreeCell, TreeView}
import javafx.util.Callback

import scala.collection.JavaConversions

/**
  * Created by Steve on 22.01.2016.
  */
object TvUtils {

  def mkTreeCellFactory[T](f: TreeView[T] => TreeCell[T]): Callback[TreeView[T], TreeCell[T]] = {
    new Callback[TreeView[T], TreeCell[T]] {
      override def call(param: TreeView[T]): TreeCell[T] = f(param)
    }
  }


  def mkNewCell[T](tToString: T => String)(tv: TreeView[T]): FileTreeCell[T] = {

    class newCell extends FileTreeCell[T] {
      override def updateItem(t: T, empty: Boolean): Unit = {
        super.updateItem(t, empty)
        if (t != null) {
          setText(tToString(t))
        }
        else setText(null)
      }
    }
    new newCell()
  }

  //----------------------Tableview-----------------------

  type TCDF[S, T] = TableColumn.CellDataFeatures[S, T]

  import JavaConversions._

  def mkObservableList[T](collection: Iterable[T]): ObservableList[T] = {
    FXCollections.observableList(new java.util.ArrayList[T](collection))
  }

  private def mkCellValueFactory[S, T](fn: TCDF[S, T] => ObservableValue[T]): Callback[TCDF[S, T], ObservableValue[T]] = {
    new Callback[TCDF[S, T], ObservableValue[T]] {
      def call(cdf: TCDF[S, T]): ObservableValue[T] = fn(cdf)
    }
  }

  def initTableViewColumnCellValueFactory[S, T](tc: TableColumn[S, T], f: S => Any): Unit = {
    tc.setCellValueFactory(mkCellValueFactory(cdf => f(cdf.getValue).asInstanceOf[ObservableValue[T]]))
  }

}
