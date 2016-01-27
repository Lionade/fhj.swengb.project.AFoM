package fhj.swengb.project.afom

import java.awt.event.MouseEvent
import java.io
import java.nio.file.{Path, Paths, Files}
import java.io.{IOException, File}
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventHandler
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.control.{MenuItem, ContextMenu, TreeCell, TextField}
import javafx.scene.input
import javafx.scene.input._


/**
  * Created by Steve on 22.01.2016.
  */
object Global {
  var zwAblage: Path = null
  var cutFlag: Boolean = false
  val dragIndex = new SimpleIntegerProperty(-1)
}


// TODO: Namen nicht durch vollen Path ersetzen nach bearbeitung
class FileTreeCell[File] extends TreeCell[File]{
  import Global._
  var txtField: TextField = _
  var cm: ContextMenu = new ContextMenu()

  /**
    * zum Umbenennen von Items
    */
  var menuRename = new MenuItem("Umbenennen")
  menuRename.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      startEdit
    }
  })

  /**
    * zum Kopieren von Items
    */
  var menuCopy = new MenuItem("Kopieren")
  menuCopy.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit ={
      zwAblage = Paths.get(getItem.toString)
    }

  })

  /**
    * zum Einfügen von Items
    */
  var menuPaste = new MenuItem("Einfügen")
  menuPaste.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit =  {
      if (zwAblage != null){
        val destination = Paths.get(getItem.toString)
        FileSystemModel.copy(zwAblage, destination)
        if (cutFlag){
          FileSystemModel.removeRecursive(zwAblage)
          cutFlag = false
        }
      }

    }
  })

  /**
    * zum Ausschneiden von Items
    */
  var menuCut = new MenuItem("Ausschneiden")
  menuCut.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      cutFlag = true
      zwAblage = Paths.get(getItem.toString)
    }
  })

  /**
    * zum Löschen von Items
    */
  var menuRemove = new MenuItem("Löschen")
  menuRemove.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      FileSystemModel.removeRecursive(Paths.get(getItem.toString))
    }
  })

  cm.getItems().addAll(menuRename,menuCopy,menuPaste,menuCut, menuRemove)


  /**
    * Cell wechselt auf änderbaren Zustand; start bei Doppel-klick
    */
  override def startEdit: Unit ={
    super.startEdit()
    if(txtField == null) createTextField()
    setText(null)
    setGraphic(txtField)
    txtField.selectAll()

  }

  // Cell schließt änderbaren Zustand
  override def cancelEdit: Unit = {
    super.cancelEdit
    setText(getString)
    setGraphic(getTreeItem.getGraphic)
  }

  /**
    * Falls File verändert wird in TextField reinschreiben
    * @param item
    * @param empty
    */
  override def updateItem(item: File, empty: Boolean): Unit ={
    super.updateItem(item, empty)
    if(empty){ // Wenn Item leer Text und Anzeige auf null setzen
      setText(null)
      setGraphic(null)
    }
    else{
      if(isEditing){
        if(txtField != null){
          txtField.setText(getString)
        }
        setText(null)
        setGraphic(txtField)
      }
      else{
        setText(getString)
        setGraphic(getTreeItem.getGraphic)
      //  setContextMenu(cm)
      }
    }
  }

  /**
    * Änderung des TreeItems bei Enter auf neuen Wert und bei Escape auf alten
    */
  def createTextField() = {
    txtField = new TextField(getString)
    txtField.setOnKeyReleased(new EventHandler[input.KeyEvent] {

      override def handle(event: input.KeyEvent): Unit = {
        if(event.getCode == KeyCode.ENTER) {
          val oldItem = getItem
          commitEdit(new java.io.File(txtField.getText()).asInstanceOf[File])
          Files.move(Paths.get(oldItem.toString), Paths.get(getItem.toString)) // Errorhandling falls von anderer Datei benützt wird
          //FileSystemModel.move(Paths.get(oldItem.toString), Paths.get(getItem.toString)) //Überschreibung
        }
        else if(event.getCode == KeyCode.ESCAPE) cancelEdit()
      }
    })
  }

  setOnDragDetected(new EventHandler[input.MouseEvent] {
    override def handle(event: input.MouseEvent): Unit = {
      if(!isEmpty){
        println("dragDetected")
        dragIndex.set(getIndex) //Eigenen Index hineinspeichern
        zwAblage = Paths.get(getItem.toString)
        val db = startDragAndDrop(TransferMode.MOVE)

        val cc = new ClipboardContent
        cc.putString(getItem.toString)
        db.setContent(cc)
      }
    }
  })

  setOnDragOver(new EventHandler[input.DragEvent] {
    override def handle(event: input.DragEvent): Unit = {
      if(dragIndex.get() >= 0 && dragIndex.get() != getIndex && getItem.asInstanceOf[java.io.File].isDirectory) {
        event.acceptTransferModes(TransferMode.MOVE)
      }
    }
  })


  setOnDragEntered(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = {
      if(dragIndex.get() >= 0 && dragIndex.get() != getIndex && getItem.asInstanceOf[java.io.File].isDirectory) {
        setStyle("-fx-background-color: gold;")
      }
    }
  })

  setOnDragExited(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = {
      setStyle("")
    }
  })

  setOnDragDropped(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = {
      if (zwAblage != null){
        FileSystemModel.copy(zwAblage, Paths.get(getItem.toString))
        FileSystemModel.removeRecursive(zwAblage)
      }
    }
  })

  setOnDragDone(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = {
      dragIndex.set(-1)
      // Refresh
    }
  })





  def getString: String = {
    if(getItem == null) "" else getItem.toString
  }
}
