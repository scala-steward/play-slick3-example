package models

import scala.concurrent.Future
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

case class Project(id: Long, name: String)

class ProjectDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  private val Projects = TableQuery[ProjectsTable]

  def findById(id: Long): Future[Project] =
    dbConfig.db.run(Projects.filter(_.id === id).result.head)

  def findByName(name: String): Future[Option[Project]] =
    dbConfig.db.run(Projects.filter(_.name === name).result.headOption)

  /*
  def findTasks(id: Long): List[Task] =
    Tasks
      .filter(_.project === id)
      .list
  */

  def all(): Future[Seq[Project]] = dbConfig.db.run(Projects.result)

  def insert(Project: Project): Future[Long] =
    dbConfig.db.run(Projects returning Projects.map(_.id) += Project)

  private class ProjectsTable(tag: Tag) extends Table[Project](tag, "PROJECT") {

    def id = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    def name = column[String]("NAME")

    def * = (id, name) <> (Project.tupled, Project.unapply _)
    def ? = (id.?, name.?).shaped.<>({ r => import r._; _1.map(_ => Project.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

  }
}