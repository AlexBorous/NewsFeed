import akka.actor.{Actor, ActorRef, PoisonPill}

class Client(var username: String, server: ActorRef,category_index: Int,alreadySigned:Boolean) extends Actor {
  if(alreadySigned)
    server ! SignInClient(username)
  else
    server !SignUpClient(username,category_index)

  def receive: Receive = {

    case Disconnect => self ! PoisonPill
    case ServerMessage(message)=> println(username+": "+self.toString()+message)
    case ServerArticles(article,title) =>println("From Server to "+username+":"+self.toString()+"\n*-------------*\n"+title+"\n"+article+"\n*-------------*")
    case NotSignedIn(username,topics) => println(f"$username you haven't sign up. You have to sign up first to view our feed .")

    case UserExists => {
      println(f" * $username already exists . Select a different name . *")
      val selection = scala.io.StdIn.readLine()
      server!SignUpClient(selection,category_index)
      username = selection
    };



  }
}

class Producer(var username: String ,server:ActorRef,category_index: Int,alreadySigned:Boolean,ready:Boolean) extends Actor{

    if (alreadySigned)
      server ! SignInProducer(username)
    else
      server ! SignUpProducer(username, User(category_index, self))


  def createNewAricle():Article = {
    println("Type the title:")
    val title = scala.io.StdIn.readLine()
    println("Type the article")
    val body = scala.io.StdIn.readLine()
     Article(body,category_index,title)
  }

  override def receive ={
    case NewArticle(title,body,author,category)=> {
      println(username+": "+self.toString()+f"Here is a breakdown of your article:\n $title \n$body \n By:$author")

      val article =  "\n"+ body + "\n"+"By:"+author
      server ! SendArticleToServer(Article(article,category,title),username)
    }
    case ServerMessage(message)=>println("Producer:"+self.toString()+message)
    case Disconnect => self ! PoisonPill
    case NotSignedIn(username,topics) => {
      println(username+": "+self.toString()+f"$username you haven't sign up . You have to sign up first to write articles.")
    };
    case UserExists => {
      println(username+": "+self.toString()+f" * $username already exists . Select a different name . *")
      val selection = scala.io.StdIn.readLine()
      server!SignUpProducer(selection,User(category_index,self))
      username = selection
    };
    case SignInComplete=> {
      println(username+": "+self.toString()+"You are connected.Creating an article ...")
      //FOR TESTING ONLY , if i have a ready article , not to communicate with the console.
      if(!ready) {
        val new_article = createNewAricle()
        self ! NewArticle(new_article.title, new_article.article, username, category_index)
      }

    }

    case MyArticles => server ! ProducersArticles(username)
  }
}