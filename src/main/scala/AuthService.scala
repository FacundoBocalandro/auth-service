import auth_service.AuthServiceGrpc.{AuthService, AuthServiceStub}
import auth_service.{AuthServiceGrpc, GetAuthenticationResponse, Status, User}
import io.grpc.{ManagedChannelBuilder, ServerBuilder}

import scala.io._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class MyService extends AuthService {
  val locationDatabase = new CSVReader()

  override def authentication(request: User): Future[GetAuthenticationResponse] = {
    val reply = GetAuthenticationResponse(status = locationDatabase.authenticate(request))
    Future.successful(reply)
  }
}

trait LocationDatabase {
  def authenticate(user: User): Status

}

class CSVReader extends LocationDatabase {

  import CSVReader._

  val data: List[User] = getUsers("users.csv")

  def authenticate(user: User): Status = {
    data.find(_ == user) match {
      case Some(_) => Status.SUCCESS
      case None => Status.FAIL
    }
  }
}

object CSVReader {

  def getUsers(filePath: String): List[User] = {
    val fileSource = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath))
    val data: List[List[String]] = fileSource.getLines().toList.map(_.split(',').toList)
    fileSource.close()
    data.map {
      case List(userMail, userPassword) =>
        User(email = userMail, password = userPassword)
    }
  }

}

object AuthServiceServer extends App {
  val builder = ServerBuilder.forPort(50000)

  builder.addService(
    AuthService.bindService(new MyService(), ExecutionContext.global)
  )

  val server = builder.build()
  server.start()

  println("Running....")
  server.awaitTermination()
}

object ClientDemo extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global


  def createStub(ip: String, port: Int = 50000): AuthServiceStub = {
    val builder = ManagedChannelBuilder.forAddress(ip, port)
    builder.usePlaintext()
    val channel = builder.build()

    AuthServiceGrpc.stub(channel)
  }

  val stub1 = createStub("127.0.0.1", 50000)
  val stub2 = createStub("127.0.0.1", 50001)

  val stubs = List(stub1, stub2)
  val healthyStubs = stubs

  val response: Future[GetAuthenticationResponse] = stub1.authentication(User(email = "facundo.bocalandro@ing.austral.edu.ar", password = "skrrrt")) //valid
//  val response: Future[GetAuthenticationResponse] = stub1.authentication(User(email = "elpepe@hot.com", password = "pepe")) //invalid

  response.onComplete { r =>
    println("Response: " + r)
  }


  System.in.read()
}