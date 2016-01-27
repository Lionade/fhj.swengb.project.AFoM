package fhj.swengb.project.afom

import java.io.File
import java.nio.file.Files._
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributeView
import javafx.beans.property.{SimpleIntegerProperty, SimpleStringProperty}

import com.sun.deploy.util.ArrayUtil

/**
 * Created by Hoxha on 22.01.2016.
 */
/**
 * domain object
 */
case class FileAttributes(name: String, modified: String, size: Int)

/**
 * domain object, but usable with javafx
 */
class MutableFileAttributes {

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
object MutableFileAttributes {

  def apply(a: FileAttributes): MutableFileAttributes = {
    val ma = new MutableFileAttributes
    ma.setName(a.name)
    ma.setModified(a.modified)
    ma.setSize(a.size)
    ma
  }
}

/**
 * simulates a database for example
 */
object DataSource {

  def addFiles(files: Array[File]): Array[FileAttributes] = {
    if(files != null)
      files.map(f => FileAttributes(if(f.isDirectory){"> " + f.getName()} else f.getName(),
        getFileAttributeView(Paths.get(f.toString()),
          classOf[BasicFileAttributeView]).readAttributes().lastModifiedTime().toString.take(19).replace("T", " "),
        (f.length()/1024).toInt))

    else
      Array[FileAttributes]()
  }
}