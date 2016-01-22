package fhj.swengb.project.afom

import java.awt.event.KeyEvent
import javafx.beans.property.{SimpleDoubleProperty, SimpleStringProperty, SimpleIntegerProperty}
import scala.reflect._
import java.io.{IOException, File}
import java.net.URL
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._
import java.util.ResourceBundle
import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.collections.{ObservableList, FXCollections}
import javafx.event.EventHandler
import javafx.fxml.{FXML, Initializable, FXMLLoader}
import javafx.scene.control._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{KeyCode, MouseButton, ContextMenuEvent, MouseEvent}
import javafx.scene.layout._
import javafx.scene.{input, Scene, Parent}
import javafx.stage.Stage
import javafx.util.Callback
import javax.activation.MimetypesFileTypeMap



import scala.collection.JavaConversions
import scala.io.Source
import scala.reflect.ClassTag
import scala.util.Random
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
  @FXML var scrollpane: ScrollPane = _
  @FXML var image: ImageView = _
  @FXML var textfield: TextArea = _

  import JfxUtils._

  type ArticleTC[T] = TableColumn[MutableArticle, T]

  @FXML var tableView: TableView[MutableArticle] = _

  @FXML var columnName: ArticleTC[String] = _
  @FXML var columnModified: ArticleTC[String] = _
  @FXML var columnSize: ArticleTC[Int] = _

  val mutableArticles = mkObservableList(DataSource.data.map(MutableArticle(_)))

  /**
    * provide a table column and a generator function for the value to put into
    * the column.
    *
    * @tparam T the type which is contained in the property
    * @return
    */
  def initTableViewColumn[T]: (ArticleTC[T], (MutableArticle) => Any) => Unit =
    initTableViewColumnCellValueFactory[MutableArticle, T]


  val rootItem = createNode(new File("c:/"))
  rootItem.setExpanded(true)
  val tree = new TreeView[File](rootItem)

  def createNode(f: File): TreeItem[File] = {
    new TreeItem[File](f){
      var isLeafx: Boolean = _
      var isFirstTimeChildren: Boolean = true
      var isFirstTimeLeaf = true

      // Beim ersten mal: setzen der Kinder; danach immer nur Rückgabe der Kinder
      override def getChildren: ObservableList[TreeItem[File]] = {
        if(isFirstTimeChildren){
          isFirstTimeChildren = false
          super.getChildren.setAll(buildChildren(this))
        }
        super.getChildren()
      }

      // Beim ertsen Mal Aufruf ob es sich um Leaf handelt; danach immer nur Rückgabe ob ja oder nein
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
            children.add(createNode(childFile)) // Rekursiv
          }
          return children
        }
        FXCollections.emptyObservableList()
      }
    }
  }


  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    import TvUtils._

    tableView.setItems(mutableArticles)

    initTableViewColumn[String](columnName, _.nameProperty)
    initTableViewColumn[String](columnModified, _.modifiedProperty)
    initTableViewColumn[Int](columnSize, _.sizeProperty)

    tree.setId("TreeView")
    tree.setEditable(true)
    tree.setCellFactory(mkTreeCellFactory(mkNewCell[File](fileToString(_))))
    tree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEvent) //throughs null pointer exceptions
    scrollpane.setContent(tree)
  }

  def mouseClickedEvent[_ >:MouseEvent] = new EventHandler[MouseEvent](){
    def handle(event: MouseEvent): Unit = {
      val fileDirectory: TreeItem[File] = tree.getSelectionModel.getSelectedItem
      event.getButton match{
        case MouseButton.PRIMARY =>
          if (fileDirectory != null && fileDirectory.isLeaf){
            val fullPath:File = fileDirectory.getValue
            val fileCategory  = FileCategory(fullPath)
            fileCategory match {
              case "image" =>
                image.setImage(new Image(fullPath.toURI.toString))
                image.setVisible(true)
                textfield.setVisible(false)
                tableView.setVisible(false)
              case "text" =>
                var text = ""
                val bufferedSource = Source.fromFile(fullPath)
                for (line <- bufferedSource.getLines()){
                  text = text + "\n" + line.toString
                }
                bufferedSource.close
                textfield.setText(text)
                image.setVisible(false)
                tableView.setVisible(false)
                textfield.setVisible(true)
              case _ =>
                println("Tableview anzeigen")
                image.setVisible(false)
                textfield.setVisible(false)
            }
          }else {
            println("Tableview anzeigen")
            tableView.setVisible(true)
          }
        case MouseButton.SECONDARY => printf("rechts-klick")
      }

    }
  }



  def FileCategory(file: File):String = {
    if(textTypes.exists(file.getName.contains(_))) {
      "text"
    }
    else if (imageTypes.exists(file.getName.contains(_))){
      "image"
    }
    else{
      "nothing"
    }
  }

  lazy val textTypes: List[String] = List(".txt", ".css", ".html", ".log", ".cfg", ".config", ".scala", ".java" , ".html", ".xml", ".fxml", ".csv", ".xhtml", ".json", ".css", ".md")
  lazy val imageTypes: List[String] = List (".jpg", ".png", ".ico", ".svg", ".bmp", ".gif", ".JPG")

}

/**
  * domain object
  */
case class Article(name: String, modified: String, size: Int)

/**
  * domain object, but usable with javafx
  */
class MutableArticle {

  val nameProperty: SimpleStringProperty = new SimpleStringProperty()
  val modifiedProperty: SimpleStringProperty = new SimpleStringProperty()
  val sizeProperty: SimpleIntegerProperty = new SimpleIntegerProperty()

  def setName(name: String) = nameProperty.set(name)

  def setModified(modified: String) = modifiedProperty.set(modified)

  def setSize(size: Int) = sizeProperty.set(size)
}


/**
  * companion object to get a better initialisation story
  */
object MutableArticle {

  def apply(a: Article): MutableArticle = {
    val ma = new MutableArticle
    ma.setName(a.name)
    ma.setModified(a.modified)
    ma.setSize(a.size)
    ma
  }
}


/**
  * util functions to bridge the javafx / scala gap
  */
object JfxUtils {

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

/**
  * simulates a database for example
  */
object DataSource {

  val data =
    (1 to 10) map {
      case i => Article("name $i", "halo $i", i)
    }

}