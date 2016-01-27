package fhj.swengb.project.afom

import java.nio.file.{Path, Paths, Files}
import javafx.beans.property.SimpleIntegerProperty
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

  //Context Einträge
  var menuRename = new MenuItem("Umbenennen")
  menuRename.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      startEdit
    }
  })

  var menuCopy = new MenuItem("Kopieren")
  menuCopy.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit ={
      zwAblage = Paths.get(getItem.toString)
    }

  })

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

  var menuCut = new MenuItem("Ausschneiden")
  menuCut.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      cutFlag = true
      zwAblage = Paths.get(getItem.toString)
    }
  })

  var menuRemove = new MenuItem("Löschen")
  menuRemove.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      FileSystemModel.removeRecursive(Paths.get(getItem.toString))
    }
  })

  var createDir = new MenuItem("Ordner erstellen")
  createDir.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      FileSystemModel.createDir(Paths.get(getItem.toString, "Neuer Ordner"))
    }
  })

  createDir.setVisible(false)
  cm.getItems().addAll(createDir, menuRename,menuCopy,menuPaste,menuCut, menuRemove)

  // Cell wechselt auf änderbaren Zustand; start bei Doppel-klick
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
    setText(getItem.asInstanceOf[java.io.File].getName)
    setGraphic(getTreeItem.getGraphic)
  }

  // Falls File verändert wird in TextField reinschreiben
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
        if(getItem.asInstanceOf[java.io.File].isDirectory) {
          createDir.setVisible(true) // Menüitem wird nur bei Directory angezeigt
          setContextMenu(cm)
        }
        else{
          createDir.setVisible(false)
          setContextMenu(cm)
        }
      }
    }
  }

  // Änderung des TreeItems bei Enter auf neuen Wert und bei Escape auf alten
  def createTextField() = {
    txtField = new TextField(getString)
    txtField.setOnKeyReleased(new EventHandler[input.KeyEvent] {

      override def handle(event: input.KeyEvent): Unit = {
        if(event.getCode == KeyCode.ENTER) {
          val oldItem = getItem
          commitEdit(new java.io.File(txtField.getText()).asInstanceOf[File])
          Files.move(Paths.get(oldItem.toString), Paths.get(getItem.toString))
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
        getTreeItem.setExpanded(true)
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
