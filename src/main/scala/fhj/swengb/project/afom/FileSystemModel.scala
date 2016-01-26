package fhj.swengb.project.afom

import java.io.{IOException, File}
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import scala.io.Source

/**
 * Created by Hoxha on 15.01.2016.
 */
object FileSystemModel {

  def move(sourcePath: Path, destinationPath: Path): Unit ={
    try{
      Files.move(sourcePath, destinationPath,
        StandardCopyOption.REPLACE_EXISTING)
    } catch{
      case e: IOException => println("something went wrong")
    }
  }

  def copy (sourcePath: Path, destinationPath: Path): Unit={
    try {
      Files.walkFileTree(sourcePath, new SimpleFileVisitor[Path]() {
        override def visitFile(sourcePath: Path, attrs: BasicFileAttributes): FileVisitResult = {
          val destinationFullPath: Path = Paths.get(destinationPath.toString + "\\" + sourcePath.getFileName.toString)
          Files.copy(sourcePath, destinationFullPath)
          FileVisitResult.CONTINUE
        }
      })
    } catch{
      case e: FileAlreadyExistsException => println("FileAlreadyExists")
      case e: IOException => println ("something else went wrong")
    }
  }

  def showRecursive(path: Path): Unit = {
    Files.walkFileTree(path, new SimpleFileVisitor[Path]() {
      override def visitFile(path: Path, attrs: BasicFileAttributes): FileVisitResult = {
        println(path)
        FileVisitResult.CONTINUE
      }

      /* override def visitFileFailed(path: Path, exc: IOException): FileVisitResult = {
        // Files.delete(path)
         println("Hallo")
         FileVisitResult.CONTINUE;
       }

       override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
         if (exc == null) {
           Files.delete(dir)
           FileVisitResult.CONTINUE
         } else {
           throw exc
         }
       }*/
    }
    )
  }
}