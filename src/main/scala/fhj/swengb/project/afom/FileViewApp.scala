package fhj.swengb.project.afom

import java.io.File
import java.nio.file
import java.nio.file.{Path, Paths}
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.shape.Path
import javafx.scene.shape.Path

import javafx.scene.{Scene, Parent}
import javafx.stage.Stage

import scala.util.control.NonFatal

/**
  * Created by Steve on 05.01.2016.
  */
object FileViewApp {
  def main(args: Array[String]) {
    Application.launch(classOf[FileViewApp], args: _*)


    //FileSystemModel.removeRecursive(path)
    //
     val sourcePath = Paths.get("C:/test1")
     val destinationPath  = Paths.get("C:/test2")
     FileSystemModel.move(sourcePath, destinationPath)
   // FileSystemModel.removeRecursive(Paths.get("C:/test1"))


  }
}


class FileViewApp extends javafx.application.Application {

  val loader = new FXMLLoader(getClass.getResource("/fhj/swengb/project/afom/FileViewer.fxml"))

  override def start(stage: Stage): Unit =
    try {
      stage.setTitle("TableView Example App")
      loader.load[Parent]()
      stage.setScene(new Scene(loader.getRoot[Parent]))
      stage.show()
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }
}
