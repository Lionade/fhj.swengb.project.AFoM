package fhj.swengb.project.afom

import javafx.event.EventHandler
import javafx.scene.control.{TreeCell, TextField}
import javafx.scene.input
import javafx.scene.input.KeyCode

/**
  * Created by Steve on 22.01.2016.
  */
class TreeCellImpl[File] extends TreeCell[File]{
  var txtField: TextField = _

  override def startEdit: Unit ={
    super.startEdit()
    if(txtField == null) createTextField()
    setText(null)
    setGraphic(txtField)
    txtField.selectAll()

  }

  override def cancelEdit: Unit = {
    super.cancelEdit
    setText(getString)
    setGraphic(getTreeItem.getGraphic)
  }

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


  def createTextField() = {
    txtField = new TextField(getString)
    txtField.setOnKeyReleased(new EventHandler[input.KeyEvent] {

      override def handle(event: input.KeyEvent): Unit = {
        if(event.getCode == KeyCode.ENTER) commitEdit(new java.io.File(txtField.getText()).asInstanceOf[File])
        else if(event.getCode == KeyCode.ESCAPE) cancelEdit()
      }
    })
  }


  def getString: String = {
    return if(getItem == null) "" else getItem.toString
  }
}
