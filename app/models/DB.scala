package models

import sorm._

object DB extends Instance(entities = Seq(Entity[InputLine]()), url = "jdbc:h2:mem:test" ) {

}
