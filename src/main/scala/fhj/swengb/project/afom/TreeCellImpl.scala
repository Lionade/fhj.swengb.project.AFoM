package fhj.swengb.project.afom

import java.io
import java.nio.file.{Paths, Files}
import javafx.event.EventHandler
import javafx.scene.control.{TreeCell, TextField}
import javafx.scene.input
import javafx.scene.input.KeyCode

/**
  * Created by Steve on 22.01.2016.
  */
// TODO: Namen nicht durch vollen Path ersetzen nach bearbeitung
class TxtFieledCell[File] extends TreeCell[File]{
  var txtField: TextField = _

  def test: Unit = {}

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
    if(empty){
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



  def getString: String = {
    if(getItem == null) "" else getItem.toString
  }
}
