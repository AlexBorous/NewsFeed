import java.util
import java.util.concurrent.ConcurrentHashMap

import akka.actor.{Actor, Terminated}

import scala.collection.mutable.ListBuffer

class Server extends Actor {
  var sport_news: ListBuffer[News] = ListBuffer[News]()
  var economical_news: ListBuffer[News] = ListBuffer[News]()
  var food_news: ListBuffer[News] = ListBuffer[News]()
  var clients = new ConcurrentHashMap[String,User]()
  var producers = new ConcurrentHashMap[String,ListBuffer[Article]]()
  val topics: Array[ListBuffer[News]] =Array(sport_news,economical_news,food_news)
  val topics_strings: Array[String] = Array("Sports","Economy","Food")
  def sendUsersArticle(article: Article): Unit = {
    val valueIterator: util.Iterator[User] =clients.values().iterator()

    while (valueIterator.hasNext)
      {
        val user =valueIterator.next()
        if(user.category == article.category)
             user.actorRef ! ServerArticles(article.article,article.title)
      }

  }
  def sendUserArticles(user: User): Unit ={
    val iterator: Iterator[News] =topics(user.category).iterator
    while(iterator.hasNext)
      {
        val it = iterator.next()
        user.actorRef!ServerArticles(it.article,it.title)
      }
  }

  def receive: Receive = {
    case SignInClient(username) => {
      if(clients.containsKey(username)) {
        //replace the old actor reference with the new one
        val topic = clients.get(username).category
        val old_user = clients.get(username)
        val new_user = User(topic,sender)
        clients.replace(username,old_user,new_user)
        sendUserArticles(new_user)
      }
      else
        sender ! NotSignedIn(username,topics_strings)
      context.watch(sender)
    };
    case SignInProducer(username) =>
      {
        if(producers.containsKey(username)) {
          sender!SignInComplete
        }
        else
          sender ! NotSignedIn(username,topics_strings)
        context.watch(sender)
     } ;
    case SignUpClient(username,category_index)=>{
      if(clients.containsKey(username))
        sender ! UserExists
      else {
        clients.put(username, User(category_index, sender))
        sender ! ServerMessage(f"---Server---Hello $username , welcome to the NewsFeed app.Here is a list of articles for ${topics_strings(category_index)} category:")
        sendUserArticles(User(category_index,sender()))
        context.watch(sender)
      }

    };
    case SignUpProducer(username,user)=>{
      if(producers.containsKey(username))
        sender ! UserExists
      else {
        val temp :ListBuffer[Article] = ListBuffer[Article]()
        producers.put(username,temp)
        sender ! ServerMessage(f"---Server---Hello $username , welcome to the NewsFeed app.You can start writing your articles for the ${topics_strings(user.category)} category.")
        sender !SignInComplete
        context.watch(sender)
      }
    };
    case SendArticleToServer(article,user)=>{
      topics(article.category) += News(article.article,article.title)
      sendUsersArticle(article)
      producers.get(user).append(article)
    }
    case Terminated(client) => context.unwatch(client)
    case ProducersArticles(username) => println("---Server---Here is the BufferList of your articles : "+producers.get(username).toString())
  }



}