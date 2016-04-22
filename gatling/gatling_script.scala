

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._



import java.text.SimpleDateFormat
import java.util.{Locale, TimeZone}
import java.util.Calendar
import java.text.SimpleDateFormat



import java.util.UUID;


import java.io._
import org.apache.commons._
import org.apache.http._
import org.apache.http.client._
import org.apache.http.client.methods.HttpPost

import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.ArrayList
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity

/** 
  *This class does the test by spraying a numberOfMsgsx10 of http post requests to your Micro-services (/messages) Concurrently
  * the class also verifies that your synthesis is correct, 
  * calculates how much time the simulation took
  * and sends the results to the leaderBoard 
  * 
  */

class Injections extends Simulation {

	//this array contains the maximum value sent for each sensorType
	var maxValues = Array(0,0,0,0,0,0,0,0,0,0)
	//this array contains the minimum value sent for each sensorType
	var minValues = Array(0,0,0,0,0,0,0,0,0,0)
	//this array contains the average of all the values sent to each sensorType
	var avrgeValues = Array(0,0,0,0,0,0,0,0,0,0)
	//the number of messages sent by a single injector 
	var numberOfMsgs = 10 000


	val httpProtocol = http
		.baseURL("http://gatling.io")
		.inferHtmlResources()
		

	val header = Map(
		"Accept" -> "text/html; charset=UTF-8 ",
		"Upgrade-Insecure-Requests" -> "1")

    //this is the adress of the http post you should put your local server
    var url="you should put the adress of your server here"+"/messages"
	

	//the Date formatter who makes the date on the DateTime RFC3339
	val formatter  = new java.text.SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS Z")
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	

	//the simulation start in nano seconds
	var simulationStartTime=0.0
	the simulation end in nano seconds
	var simulationEndTime=0.0
	//the name of your team
	var teamName=""
	//your location (Nantes, Paris, Brest etc...)
	var teameLocation=""

	

	//unique Id generator for each message sent
	def generateId():String={
		UUID.randomUUID().toString()+UUID.randomUUID().toString()
	}
    
    //generate random numbers and keep the min the max and the average 
    def generateNum(sensorTypeIndex:Int):Int={

		val r = scala.util.Random
		var value = r.nextInt

		if(value > maxValues(sensorTypeIndex)){
			maxValues(sensorTypeIndex) = value
		}else if (value < minValues(sensorTypeIndex)){
			minValues(sensorTypeIndex) = value	
		}

		//this adds values as they are created to be able to get the average at the end of the simulation(numberOfMsgs)
		avrgeValues(sensorTypeIndex) = avrgeValues(sensorTypeIndex) + value

		return value

	}
    
   /** This Bloc of code runs before the simulation
  	* it retreives the name of the team,the location and the start time of the simulation 
  	*/
	before {
    		println("la simulation est sur le point de commencer...")
    		//add your team name here this name will appear on the leaderBoard
    		teamName=""
    		//add your location here, it will also appear on the leaderBoard
    		teameLocation=""
    		simulationStartTime=System.nanoTime()		
  	}

  	
  	/** now we construct 10 scenarios that send each numberOfMsgs to our microservice and verifies that the message was sent...
  	*these scenarios are run concurently, the total output of messages is numberOfMsgs*10
  	*/


  	//scneario 1 
	val scn1 = scenario("injecteur 1").repeat(numberOfMsgs){ 
				exec(http("request_1")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
						                      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"1",
								      "value":""""+generateNum(0)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 2
	val scn2 = scenario("injecteur 2").repeat(numberOfMsgs){  
				exec(http("request_2")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"2",
								      "value":""""+generateNum(1)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 3
	val scn3 = scenario("injecteur 3").repeat(numberOfMsgs){  
				exec(http("request_3")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"3",
								      "value":""""+generateNum(2)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}
	

	//scenario 4

	val scn4 = scenario("injecteur 4").repeat(numberOfMsgs){ 	    
				exec(http("request_4")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"4",
								      "value":""""+generateNum(3)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 5

	val scn5 = scenario("injecteur 5").repeat(numberOfMsgs){ 
				exec(http("request_5")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"5",
								      "value":""""+generateNum(4)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 6
	val scn6 = scenario("injecteur 6").repeat(numberOfMsgs){ 
				exec(http("request_6")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"6",
								      "value":""""+generateNum(5)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 7
	val scn7 = scenario("injecteur 7").repeat(numberOfMsgs){ 
				exec(http("request_7")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"7",
								      "value":""""+generateNum(6)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 8
	val scn8 = scenario("injecteur 8").repeat(numberOfMsgs){
				exec(http("request_8")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"8",
								      "value":""""+generateNum(7)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 9

	val scn9 = scenario("injecteur 9").repeat(numberOfMsgs){ 
				exec(http("request_9")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"9",
								      "value":""""+generateNum(8)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)		
	}

	//scenario 10
	val scn10 = scenario("injecteur 10").repeat(numberOfMsgs){ 
				exec(http("request_10")
					.post(url)
					.body(StringBody(session=>"""{"id":""""+generateId()+"""",
								      "timestamp":""""+formatter.format(Calendar.getInstance().getTime())+"""",
								      "sensorType":"10",
								      "value":""""+generateNum(9)+"""" }""")).asJSON
					.headers(header)
					.check(status.is(200))
					)								
     }


	/**we run the scenarios and assert that 100% 
  	*of messages were received
  	*/
	  setUp(scn1.inject(atOnceUsers(1)),
	  	scn2.inject(atOnceUsers(1)),
	  	scn3.inject(atOnceUsers(1)),
	  	scn4.inject(atOnceUsers(1)),
	  	scn5.inject(atOnceUsers(1)),
	  	scn6.inject(atOnceUsers(1)),
	  	scn7.inject(atOnceUsers(1)),
	  	scn8.inject(atOnceUsers(1)),
	  	scn9.inject(atOnceUsers(1)),
	  	scn10.inject(atOnceUsers(1)))
	  .protocols(httpProtocol)
	  .assertions(global.successfulRequests.percent.is(100))
	
	 

 	/** This Bloc of code runs after the simulation
  	* the end time of the simulation 
  	*it verifies that the synthesis is correct
  	* and sends the results to the leaderBoard
  	*/
	after {
		//end Time
  		simulationEndTime=System.nanoTime()

  		println("la simulation est finie traitement en cours...")
  		//this function retreives the values of a map made from a json object
  		def show(x: Option[Any],b:String) = x match {
      		case Some(m: Map[String, Any]) => m(b) match{
      		case s: String => s
      		case i: Int => i
      		case d: Double => d
      		}

      		case None =>
   		}
  		
  		//total time of the simulation in nanoseconds
  		val timeOfSimulation=simulationEndTime-simulationStartTime


		//the results checker
		var resultatValid=true

  		for( a <- 1 to 10){
  			
  			//this is the url of the synthesis get method that sends a synthesis object for each sensor Type server 
  			val urlSyhtesis ="yourserver" +"/synthesis?sensorType="+a.toString()
  			
  			val result = scala.io.Source.fromURL(urlSyhtesis)

  			val SynthesisJson = scala.util.parsing.json.JSON.parseFull(result.mkString)

	 		if(show(SynthesisJson,"minValue")==minValues(a-1) && 
	 		   show(SynthesisJson,"maxValue")==maxValues(a-1) && 
	 		   show(SynthesisJson,"mediumValue")==avrgeValues(a-1) ){

	 				println(show(SynthesisJson,"sensorType")+"results are valid")
	 				
	 		}else{
	 				println(show(SynthesisJson,"sensorType")+"les résultats ne sont pas valide!!!!")
	 				resultatValid=false

	 		}
	 		
  		}
  		
		//if the results are valid they are sent to the leaderBoard
		if(resultatValid==true){

			println("Temp d'execution:"+timeOfSimulation+" Equipe:"+teamName+" rattachement:"+teameLocation)

  			val password=scala.io.StdIn.readLine("entrez le mot de pass Pour envoyer le resultat?: ")
        	 	
        	 	val urlLeaderBoard="I should put my leaderboard here to be changed for the people to be able to send their results"

        	 	val post = new HttpPost(urlLeaderBoard)
        		val client = HttpClientBuilder.create().build()
        		
                	post.setEntity(new StringEntity("""{"teamName":""""+teamName+"""",
                					    "time":""""+timeOfSimulation+"""",
                					    "location":""""+teameLocation+"""",
                					    "password":""""+password+""""}"""))

                	val response = client.execute(post)

        		  if(response.getStatusLine().getStatusCode()==200){
                			println("votre résultat est envoyé, n'oubliez pas de regarder le leaderBoard pour voir votre classement...")
        		 }else{
                			println("L'envoi a échoué!!! le résultat n'a pas été envoyé au leaderboard!!!")
        		 }

		}else{
			println("votre synthése n'est pas valide, vous ne pouvez pas envoyer vos résultats!")
		}
	 
        	 println("traitement effectué!")  
	 }
}
