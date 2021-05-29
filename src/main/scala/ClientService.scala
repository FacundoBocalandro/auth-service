import auth_service.AuthServiceGrpc.AuthServiceStub
import auth_service.{AuthServiceGrpc, GetAuthenticationResponse, User}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object ClientService extends App {

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