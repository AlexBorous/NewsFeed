import akka.actor.ActorRef

import scala.collection.mutable.ListBuffer

abstract class Msg

case class NewArticle(title:String,body:String,author:String,category:Int)
case object Disconnect extends Msg
case class SignInClient(name:String)extends  Msg
case class NotSignedIn(msg: String,topics:Array[String]) extends Msg
case class SignInProducer(name:String) extends Msg
case class SignUpClient(name:String,category_index: Int)extends Msg
case class SignUpProducer(name:String,user: User)extends Msg
case class ServerMessage(welcomeMsg: String) extends Msg
case class ServerArticles(article: String,title:String) extends Msg
case class UserExists()extends Msg
case class SignInComplete() extends Msg
case class showNews(news:ListBuffer[News])
case class User(category:Int,actorRef: ActorRef)
case class News(article:String,title:String)
case class SendArticleToServer(article: Article,name:String)
case class Article(article: String,category: Int,title:String)
case class MyArticles() extends Msg
case class ProducersArticles(username:String) extends Msg