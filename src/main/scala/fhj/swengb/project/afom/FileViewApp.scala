package fhj.swengb.project.afom

import java.awt.Desktop
import java.io.{IOException, File}
import java.net.URL
import java.util.ResourceBundle
import javafx.application.Application
import javafx.collections.{ObservableList, FXCollections}
import javafx.event.{EventHandler}
import javafx.fxml.{FXML, Initializable, FXMLLoader}
import javafx.scene.control._
import javafx.scene.image.{ImageView, Image}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.{Scene, Parent}
import javafx.stage.Stage
import javafx.util.Callback

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

  val Fxml = "/fhj/swengb/project/afom/FileViewer.fxml"
  val Css = "/fhj/swengb/project/afom/FileViewer.css"

  //val loader = new FXMLLoader(getClass.getResource("/fhj/swengb/project/afom/FileViewer.fxml"))

  def mkFxmlLoader(fxml: String): FXMLLoader = {
    new FXMLLoader(getClass.getResource(fxml))
  }

  override def start(stage: Stage): Unit =
    try {
      stage.setTitle("AFoM - App")
      setSkin(stage, Fxml, Css)
      stage.show()
      stage.setMinWidth(stage.getWidth)
      stage.setMinHeight(stage.getHeight)
      stage.setResizable(false)
    } catch {
      case NonFatal(e) => e.printStackTrace()
    }

  def setSkin(stage: Stage, fxml: String, css: String): Boolean = {
    val scene = new Scene(mkFxmlLoader(fxml).load[Parent]())
    stage.setScene(scene)
    stage.getScene.getStylesheets.clear()
    stage.getScene.getStylesheets.add(css)
  }
}


class FileViewController extends Initializable {
  @FXML var refresh: Button = _
  @FXML var treeview: TreeView[File] = _
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


  var rootItem = createNode(new File("c:/"))
  rootItem.setExpanded(true)


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

    refresh.setGraphic(new ImageView(new Image("/fhj/swengb/project/AFoM/refresh.png")))
    columnModified.getStyleClass().add("column")
    columnSize.getStyleClass().add("column")

    initTableViewColumn[String](columnName, _.nameProperty)
    initTableViewColumn[String](columnModified, _.modifiedProperty)
    initTableViewColumn[Int](columnSize, _.sizeProperty)

    treeview.setId("TreeView")
    treeview.setEditable(true)

    treeview.setCellFactory(new Callback[TreeView[File],TreeCell[File]]() {
      override def call(p: TreeView[File]): TreeCell[File] = {
        val cell: FileTreeCell[File] = new FileTreeCell[File]
        cell
      }
    })

    tableView.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseClickedEventTableView)
    treeview.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClickedEvent) //throughs null pointer exceptions

    treeview.setRoot(rootItem)
  }


  def mouseClickedEventTableView = new EventHandler[MouseEvent] {
    def handle(event: MouseEvent): Unit = {
      if (event.getClickCount() == 2) {
        //gets fileDirectory from tree
        val fileDirectory: File = treeview.getSelectionModel.getSelectedItem.getValue
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
    def handle(event: MouseEvent): Unit = {
      val fileDirectory: TreeItem[File] = treeview.getSelectionModel.getSelectedItem
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
              image.setVisible(false)
              textfield.setVisible(false)
          }
        } else {
          tableView.setVisible(true)
          val direcory: File = fileDirectory.getValue
          if(direcory != null){
            mutableFileAttributes = mkObservableList(DataSource.addFiles(direcory.listFiles()).map(MutableFileAttributes(_)))
            tableView.setItems(mutableFileAttributes)
          }
        }
      }
    }
  }

  def onRefresh: Unit = {
    treeview.focusModelProperty()

    rootItem = createNode(new File("c:/"))
    rootItem.setExpanded(true)

    treeview.setRoot(rootItem)
    treeview.setEditable(true)

    treeview.refresh()
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


