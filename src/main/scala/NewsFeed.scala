import akka.actor.{ActorRef, ActorSystem, Props}
import akka.routing.BalancingPool

object NewsFeed{
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("System")

    val server = system.actorOf(BalancingPool(4).props(Props(new Server)))
    println("Would you like a pre-made test build (1) or test it for yourself(2)?")
    val choice = io.StdIn.readInt()
    if(choice == 2) {
      println("Welcome to the NewsFeed's Tester \nIMPORTANT: Due to limitations of testing in a single device with a multi-actor application , there is about 20s time to write an article and sign in/up , you are advised to write short articles  .\n + Server is up and running \n + To create a client press C \n + To create a producer press P \n - If you want to quit press Q")
      var input = io.StdIn.readChar()

      while (input != 'Q') {
        if (input == 'C') {
          val client = createClient(system)
          Thread.sleep(20000)
          client ! Disconnect
        }
        else if (input == 'P') {
          val producer = createProducer(system)
          Thread.sleep(20000)
          producer ! Disconnect
        }
        else
          println("*Wrong input.*")

        Thread.sleep(20000)
        println("\n + To create a client press C \n + To create a producer press P \n - If you want to quit press Q\n")
        input = io.StdIn.readChar()
      }
    }
    else if(choice == 1)
      {
          println("Program with 3 client(names=Client+index(1-3) , 1 producer(name = Producer , articles = 3) and a server.(Sports category)")
          val producer = system.actorOf(Props(new Producer("Producer", server, 0, false,true)))
          Thread.sleep(1000)
          producer ! NewArticle("Liverpool to be announced champions in Premier League.","As a lot are saying Liverpool , truly deserves this championship with the impressive form so far . \nHearing these the council has decided that is only fair to happen. ","BillTheGoalie",0)
        Thread.sleep(1000)
          val client1 = system.actorOf(Props(new Client("Client1", server, 0, false)))
          Thread.sleep(1000)
          producer ! NewArticle("Arsenal in trouble ...","With the huge amount of money spent in the summer transfer market\nArsenal continues to disappoint and Emery is in trouble. ","BillTheGoalie",0)
          producer ! NewArticle("Panathinaikos enquiry!","Famous Greek team Panathinaikos F.C is rumored to be enquired by Chinese party,\naccording to Greek journalist Georgios Georgiou","BillTheGoalie",0)
          val client2 = system.actorOf(Props(new Client("Client2", server, 0, false)))
          val client3 = system.actorOf(Props(new Client("Client3", server, 0, false)))
        Thread.sleep(4000)
      }
    else
        println("Wrong input.")


    def createProducer(system: ActorSystem): ActorRef = {
      println("Give producer a name : ")
      val name = io.StdIn.readLine()
      println("Do you already have an account?")
      val signed = io.StdIn.readLine()
      if (signed == "yes" || signed == "Yes" || signed == "YES") {
        println("What would you like to write a article for , this time ? \n+ Sports(0) \n+ Economy(1) \n+ Food(2)")
        var category = io.StdIn.readInt()
        while (category >= 3 || category < 0) {
          println("Wrong input.\nGive a number from 0 -> 2")
          category = io.StdIn.readInt()
        }
        val producer = system.actorOf(Props(new Producer(name, server, category, true,false)))
        producer
      }
      else {
        println("What would you like to write news for ? \n+ Sports(0) \n+ Economy(1) \n+ Food(2)")
        var category = io.StdIn.readInt()

        while (category >= 3 || category < 0) {
          println("Wrong input.\nGive a number from 0 -> 2")
          category = io.StdIn.readInt()
        }
        val producer = system.actorOf(Props(new Producer(name, server, category, false,false)))
        producer
      }
    }
  def createClient(system: ActorSystem): ActorRef = {
      println("Give clients name : ")
      val name = io.StdIn.readLine()
      println("Do you already have an account?")
      val signed = io.StdIn.readLine()
      if(signed == "yes"||signed == "Yes"||signed=="YES")
      {
        val client = system.actorOf(Props(new Client(name,server,1,true)))
        client
      }
      else {
        println("What would you like to read news for ? \n+ Sports(0) \n+ Economy(1) \n + Food(2)")
        var category = io.StdIn.readInt()
        while (category >= 3 || category < 0) {
          println("Wrong input.\nGive a number from 0 -> 2")
          category= io.StdIn.readInt()
        }
        val client = system.actorOf(Props(new Client(name, server, category, false)))
        client
      }
  }


    val start = system.uptime
    println("Server's uptime : "+start+" seconds.")
    println("Shutting down...")
    system.terminate()

  }
}
