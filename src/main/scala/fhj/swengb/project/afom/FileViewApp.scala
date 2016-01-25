package fhj.swengb.project.afom

import java.awt.Desktop
import java.awt.event.KeyEvent
import javafx.beans.property.{SimpleDoubleProperty, SimpleStringProperty, SimpleIntegerProperty}
import scala.collection.immutable.IndexedSeq
import scala.reflect._
import java.io.{IOException, File}
import java.net.URL
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._
import java.util.ResourceBundle
import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.collections.{ObservableList, FXCollections}
import javafx.event.{ActionEvent, EventHandler}
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

  import TvUtils._

  type FileAttributeTC[T] = TableColumn[MutableFileAttributes, T]

  @FXML var tableView: TableView[MutableFileAttributes] = _

  @FXML var columnName: FileAttributeTC[String] = _
  @FXML var columnModified: FileAttributeTC[String] = _
  @FXML var columnSize: FileAttributeTC[Int] = _

  var mutableFileAttributes: ObservableList[MutableFileAttributes] = _

  /**
   * provide a table column and a generator function for the value to put into
   * the column.
   *
   * @tparam T the type which is contained in the property
   * @return
   */
  def initTableViewColumn[T]: (FileAttributeTC[T], (MutableFileAttributes) => Any) => Unit =
    initTableViewColumnCellValueFactory[MutableFileAttributes, T]


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


    initTableViewColumn[String](columnName, _.nameProperty)
    initTableViewColumn[String](columnModified, _.modifiedProperty)
    initTableViewColumn[Int](columnSize, _.sizeProperty)

    tree.setId("TreeView")
    tree.setEditable(true)
    tree.setCellFactory(mkTreeCellFactory(mkNewCell[File](fileToString(_))))

    tableView.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseClickedEventTableView)
    tree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEvent) //throughs null pointer exceptions

    scrollpane.setContent(tree)
  }


  def mouseClickedEventTableView = new EventHandler[MouseEvent] {
    def handle(event: MouseEvent): Unit = {
      if (event.getClickCount() == 2) {
        //gets fileDirectory from tree
        val fileDirectory: File = tree.getSelectionModel.getSelectedItem.getValue
        //gets selected tableposition
        val pos = tableView.getSelectionModel().getSelectedCells().get(0)

        //gets value from tablecell
        val tableFile = pos.getTableColumn().getCellObservableValue(pos.getRow()).getValue

        // adds file directory and table file to a full path
        val fullPath = new File(fileDirectory + "\\" + tableFile)
        try{
          Desktop.getDesktop.open(fullPath)
        }
        catch{
          case e: IOException => e.printStackTrace()

        }
      }
    }
  }


  def mouseClickedEvent[_ >:MouseEvent] = new EventHandler[MouseEvent](){

    var cm: ContextMenu = new ContextMenu()

    //Context Einträge
    var menuRename = new MenuItem("Umbenennen")
    menuRename.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = {
        tree.edit(tree.getSelectionModel.getSelectedItem) //Finds current TreeItem and edits it
      }
    })

    var menuCopy = new MenuItem("Kopieren")
    menuCopy.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = println("Copy")
    })

    var menuPaste = new MenuItem("Einfügen")
    menuPaste.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = println("Paste")
    })

    var menuCut = new MenuItem("Ausschneiden")
    menuCut.setOnAction(new EventHandler[ActionEvent] {
      override def handle(event: ActionEvent): Unit = println("Cut")
    })

    cm.getItems().addAll(menuRename,menuCopy,menuPaste,menuCut)

    def handle(event: MouseEvent): Unit = {
      val fileDirectory: TreeItem[File] = tree.getSelectionModel.getSelectedItem
      event.getButton match{
        case MouseButton.PRIMARY =>
          cm.hide() //Versteckt Context Menü bei links-klick wieder
          if(fileDirectory != null) {
            if (fileDirectory.isLeaf) {
              val fullPath: File = fileDirectory.getValue
              val fileCategory = FileCategory(fullPath)
              fileCategory match {
                case "image" =>
                  image.setImage(new Image(fullPath.toURI.toString))
                  image.setVisible(true)
                  textfield.setVisible(false)
                  tableView.setVisible(false)
                case "text" =>
                  var text = ""
                  val bufferedSource = Source.fromFile(fullPath)
                  for (line <- bufferedSource.getLines()) {
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
            } else {
              println("Tableview anzeigen")
              tableView.setVisible(true)
              val direcory: File = fileDirectory.getValue
              mutableFileAttributes = mkObservableList(DataSource.addFiles(direcory.listFiles()).map(MutableFileAttributes(_)))
              tableView.setItems(mutableFileAttributes)
            }
          }
        case MouseButton.SECONDARY => printf("rechts-klick")
          cm.show(tree, event.getScreenX, event.getScreenY)
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


