package fhj.swengb.project.afom

import java.io.{IOException, File}
import java.net.URL
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, SimpleFileVisitor, Files, Path}
import java.util.ResourceBundle
import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.collections.{ObservableList, FXCollections}
import javafx.event.EventHandler
import javafx.fxml.{FXML, Initializable, FXMLLoader}
import javafx.scene.control._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{MouseButton, ContextMenuEvent, MouseEvent}
import javafx.scene.layout._
import javafx.scene.{Scene, Parent}
import javafx.stage.Stage
import javafx.util.Callback
import javax.activation.MimetypesFileTypeMap


import scala.collection.JavaConversions
import scala.io.Source
import scala.util.control.NonFatal


/**
  * Created by Steve on 05.01.2016.
  */
object FileViewApp {
  def main(args: Array[String]) {
    Application.launch(classOf[FileViewApp], args: _*)
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

class FileViewController extends Initializable {
  import JfxUtils._

  @FXML var scrollpane: ScrollPane = _
  @FXML var image: ImageView = _
  @FXML var textfield: TextArea = _

  val rootItem = createNode(new File("c:/"))
  rootItem.setExpanded(true)


  def createNode(f: File): TreeItem[File] = {
    new TreeItem[File](f){
      var isLeafx: Boolean = _
      var isFirstTimeChildren: Boolean = true
      var isFirstTimeLeaf = true


      override def getChildren: ObservableList[TreeItem[File]] = {
        if(isFirstTimeChildren){
          isFirstTimeChildren = false
          super.getChildren.setAll(buildChildren(this))
        }
        super.getChildren()
      }

      override def isLeaf(): Boolean = {
        if(isFirstTimeLeaf){
          isFirstTimeLeaf = false
          val f: File = getValue()
          isLeafx = f.isFile
        }
        isLeafx
      }

      def buildChildren(treeItem: TreeItem[File]): ObservableList[TreeItem[File]] = {
        val f: File = treeItem.getValue()
        if(f == null) FXCollections.emptyObservableList()
        if(f.isFile()) FXCollections.emptyObservableList()

        val files: Array[File] = f.listFiles()
        if(files != null){
          val children: ObservableList[TreeItem[File]] = FXCollections.observableArrayList()
          for(childFile <- files){
            children.add(createNode(childFile))
          }
          return children
        }
        FXCollections.emptyObservableList()
      }
    }
  }

  val tree = new TreeView[File](rootItem)

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    //addChilds(rootItem, new File(".").listFiles())
    tree.setId("TreeView")
    tree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEvent)
    scrollpane.setContent(tree)
  }

  def mouseClickedEvent[_ >:MouseEvent] = new EventHandler[MouseEvent](){
    def handle(event: MouseEvent): Unit ={
      val fileDirectory: TreeItem[File] = tree.getSelectionModel.getSelectedItem
      val fullPath: File = fileDirectory.getValue
      val mimeType = new MimetypesFileTypeMap().getContentType(fullPath)
      println(fileDirectory)
      println("------------")
      println(fileDirectory.isLeaf)
      if (fileDirectory.isLeaf){
        if(mimeType.substring(0,5).equalsIgnoreCase("image")){
          image.setImage(new Image(fullPath.toURI.toString))
          println("Ist ein image")
        }else {
          var text = ""
          for (line <- Source.fromFile(fullPath).getLines) {
            text = text + "\n" + line.toString
          }
          textfield.setText(text)
          println("+++++++++")
          //textfield.setText(lines)
        }
      }else {

      }
    }
  }

}

object JfxUtils{

}