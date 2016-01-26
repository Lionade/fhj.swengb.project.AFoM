package fhj.swengb.project.afom

import java.awt.event.MouseEvent
import java.io
import java.nio.file.{Paths, Files}
import javafx.event.EventHandler
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.control.{MenuItem, ContextMenu, TreeCell, TextField}
import javafx.scene.input
import javafx.scene.input.{KeyEvent, DragEvent, KeyCode}


/**
  * Created by Steve on 22.01.2016.
  */
object Global {
  var zwAblage:String = "geht net"
}


// TODO: Namen nicht durch vollen Path ersetzen nach bearbeitung
class TxtFieledCell[File] extends TreeCell[File]{
  var txtField: TextField = _
  val cm: ContextMenu = new ContextMenu()


  var menuRename = new MenuItem("Umbenennen")
  menuRename.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      startEdit
    }
  })

  var menuCopy = new MenuItem("Kopieren")
  menuCopy.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      Global.zwAblage = "test"
    }
  })

  var menuPaste = new MenuItem("Einfügen")
  menuPaste.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      println(Global.zwAblage)
    }
  })

  var menuCut = new MenuItem("Ausschneiden")
  menuCut.setOnAction(new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = println("Cut")
  })

  cm.getItems().addAll(menuRename,menuCopy,menuPaste,menuCut)


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
    setText(getString)
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
        setContextMenu(cm)
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
          Files.move(Paths.get(oldItem.toString), Paths.get(getItem.toString)) // Errorhandling falls von anderer Datei benützt wird
          //FileSystemModel.move(Paths.get(oldItem.toString), Paths.get(getItem.toString)) //Überschreibung
        }
        else if(event.getCode == KeyCode.ESCAPE) cancelEdit()
      }
    })
  }

  setOnDragDetected(new EventHandler[input.MouseEvent] {
    override def handle(event: input.MouseEvent): Unit = println("dragDetected")
  })

  setOnDragEntered(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = println("DragEntered")
  })

  setOnDragExited(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = println("DragExited")
  })

  setOnDragDropped(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = println("DragDropped")
  })

  setOnDragDone(new EventHandler[DragEvent] {
    override def handle(event: DragEvent): Unit = println("DragDone")
  })




  def getString: String = {
    if(getItem == null) "" else getItem.toString
  }
}
