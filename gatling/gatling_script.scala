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
  *This class does the test by spraying a msgPackage x numOfPackages x 10 of http post requests to your service (/messages) Concurrently
  * verfies that your synthesis service works given a timestamp and a duration
  * verifies that your synthesis for the total of values sent  is correct, 
  * calculates how much time the simulation took
  * sends the results to the leaderBoard (validate if you have the password)
  */

  class InjectionsAndVerifications extends Simulation {

  	//enter the name of your team
	var teamName=""
	//enter the members of the team
	var teamMembers=""
	//enter your location (Nantes, Paris, Brest etc...)
	var teameLocation=""

	//HTTP Protocol

	val httpProtocol = http.baseURL("http://").inferHtmlResources()	
	val header = Map("Accept" -> "text/html; charset=UTF-8 ","Upgrade-Insecure-Requests" -> "1")

    //this is the adress of the http post the adress is your rasberry pi adress
    var url="http://192.168.1.1/messages"


	//the Date formatter who makes the date on the DateTime RFC3339
	val formatter  = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ")
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	
	//the simulation start in nano seconds
	var simulationStartTime=0.0
	//the simulation end in nano seconds
	var simulationEndTime=0.0
	//start time for sending a package by sensor type
	var startTimePackage=Array(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
	//end time for sending a package by sensortype
	var endTimePackage=Array(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)


	/**
	 *this method generates a unique Id  for each message sent
	 *@param Unit
	 *@return a Unique UUID
	 */
	def generateId():String={
		UUID.randomUUID().toString().replaceAll("-", "")+UUID.randomUUID().toString().replaceAll("-", "")
	}

	//this array contains the maximum value sent for each sensorType
	var totalMaxValues = Array(0,0,0,0,0,0,0,0,0,0)
	//this array contains the minimum value sent for each sensorType
	var totalMinValues = Array(0,0,0,0,0,0,0,0,0,0)
	//this array contains the average of all the values sent to each sensorType
	var totalSumValues = Array(0,0,0,0,0,0,0,0,0,0)
	


	// number of times generateNum was called (to track the first call of the function)
	var counterNumberGenerator=Array(0,0,0,0,0,0,0,0,0,0)
	/**
	 *generates a random value and saves the minimum/maximum/sum globaly
	 *@param the Index of the sensorType 
	 *@return random value
	 */    
    def generateNum(sensorTypeIndex:Int):Int={

		val r = scala.util.Random
		var value = r.nextInt
		//first call 
		if(counterNumberGenerator(sensorTypeIndex)==0){
			totalMaxValues(sensorTypeIndex) = value
			totalMinValues(sensorTypeIndex) = value	
		}else{
			if(value > totalMaxValues(sensorTypeIndex)){
				totalMaxValues(sensorTypeIndex) = value
			}else if (value < totalMinValues(sensorTypeIndex)){
				totalMinValues(sensorTypeIndex) = value	
			}
		}
		counterNumberGenerator(sensorTypeIndex)+=1

		//this adds values as they are created to be able to get the average at the end of the simulation(msgPackage)
		totalSumValues(sensorTypeIndex) = totalSumValues(sensorTypeIndex) + value

		return value
	}


	//this array contains the maximum value sent for each sensorType within a defined time
	var partialMaxValue = Array(0,0,0,0,0,0,0,0,0,0)
	//number of times max was called withing a defined time
	var counterMax=Array(0,0,0,0,0,0,0,0,0,0)
	/**
	 *test a value compare it to an another value and saves the maximum value 
	 *@param the value + the Index of the sensorType 
	 *@return maximum
	 */  
	def maxNum(num:Int,sensorIndex:Int):Int={
		var theMax=0
		if(counterMax(sensorIndex)==0){
			theMax=num
		}else{
			if(num > partialMaxValue(sensorIndex)){
				theMax=num
			}else{
				theMax=partialMaxValue(sensorIndex)
			}
		}
		counterMax(sensorIndex)+=1
		return theMax
	}


	//this array contains the minimum value sent for each sensorType within a defined time
	var partialminValue = Array(0,0,0,0,0,0,0,0,0,0)
	//number of times min was called withing a defined time
	var counterMin=Array(0,0,0,0,0,0,0,0,0,0)
	/**
	 *test a value compare it to an another value and saves the minimum value 
	 *@param the value + the Index of the sensorType 
	 *@return minimum
	 */ 
	def minNum(num:Int,sensorIndex:Int):Int={
		var theMin=0
		if(counterMin(sensorIndex)==0){
			theMin=num
		}else{
			if(num < partialminValue(sensorIndex)){
				theMin=num
			}else{
				theMin=partialminValue(sensorIndex)
			}
		}
		counterMin(sensorIndex)+=1
		return theMin
	}

	//this array contains the average of all the values sent to each sensorType within a package
	var partialSumValue = Array(0,0,0,0,0,0,0,0,0,0)
	/**
	 *generates the json message (including the random number)
	 *save the minimum/the maximum and the sum 
	 *@param  the Index of the sensorType 
	 *@return a json formated string that will be the body of the message
	 */ 
	///json generator
	def generateJson(sensorIndex:Int):String={
		var timeNow=Calendar.getInstance().getTimeInMillis()
		var number=generateNum(sensorIndex)

		var jsonMsg="""{"id":""""+generateId()+"""",
						"timestamp":""""+formatter.format(timeNow)+"""",
						"sensorType":"""+(sensorIndex+1)+""",
						"value":"""+number+""" }"""

		partialMaxValue(sensorIndex)=maxNum(number,sensorIndex)
		partialminValue(sensorIndex)=minNum(number,sensorIndex)
		partialSumValue(sensorIndex)=partialSumValue(sensorIndex)+number

		return jsonMsg

	}


	/** This Bloc of code runs before the simulation
  	* it retreives the start time of the simulation 
  	*/
	before {
    		println("la simulation est sur le point de commencer...")
    		
    		simulationStartTime=System.nanoTime()		
  	}


  	//the number of messages in a  package
	var msgPackage = 100
	//the synthesis duration of a package of tests in miliseconds
	var duration=Array(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)

	/**
	 *this object is the scenario builder 
	 * a scenarios sends a msgPackage to our microservice and verifies that the message was sent ...
	 */ 
	object ScenarioBuilder {
		
		/**
	 	 *generates container of the synthesis result name
		 *@param  the Index of the sensorType 
		 *@return the name of the container
		 */ 

  		def SynthesisSensorNum(sensorIndex:Int):String={
  			return "SynthesisSensor"+sensorIndex.toString()
  		}

  		/**
	 	 *generates Index of the result of the synthesis
		 *@param  the Index of the sensorType, the element needed from the synthesis("sensorType","minValue","maxValue","mediumValue")
		 *@return the index in the session
		 */ 
  		def getSynthesisResultIndex(sensorIndex:Int,element:String):Int={

  			val index= element match{
  				case "sensorType"=> 1+((sensorIndex)*4)
  				case "minValue"=> 2+((sensorIndex)*4)
  				case "maxValue"=> 3+((sensorIndex)*4)
  				case "mediumValue"=> 4+((sensorIndex)*4)
  			}

  			return index
  		}

  		//constructs the request name depending on the injector
  		def requestName(sensorIndex:Int):String={
  			return "request_"+sensorIndex.toString()
  		}

  		//constructs the synthesis request name depending on the injector
  		def synthesisResultsCheck(sensorIndex:Int):String={
  			return "Sensor_Synthesis_Check "+sensorIndex.toString()
  		}


  		/**
	 	 *sends a package of messages and checks if the synthesis of the package is correct
		 *@param  the Index of the sensorType
		 *@return a scenario that sends a package of messages containing msgPackage + checks the synthesis 
		 */ 

  		def SendMsgsAndCHeckSynthesis(sensorIndex:Int)= exec(session=>{

  			//time the package of messages started sending...
			startTimePackage(sensorIndex)=Calendar.getInstance().getTimeInMillis()
			session

			})
  			//send a package of messages and pause to round the time to seconds
  			.repeat(msgPackage){  

				exec(http(requestName(sensorIndex))
					.post(url)
					.body(StringBody(session=>generateJson(sensorIndex))).asJSON
					.headers(header)
					.check(status.is(200))
					)	

			}.pause(1)

			//get the parameters of the synthesis call
			.exec(session=>{
			//time the package of messages finished sending...
			endTimePackage(sensorIndex)=Calendar.getInstance().getTimeInMillis()
			//duration that took the package to send all the messages
			duration(sensorIndex)=endTimePackage(sensorIndex)-startTimePackage(sensorIndex)
			session
			})
			//call and get the synthesis corresponding to the duration and timestamp
			.exec(http(synthesisResultsCheck(sensorIndex))
					.get("http://192.168.1.1/messages/synthesis?timestamp="+
					java.net.URLEncoder.encode(formatter.format(startTimePackage(sensorIndex)), "utf-8")+
					"&duraion="+(duration(sensorIndex)/1000).toInt)
  					.check(jsonPath("$..*").findAll.saveAs(SynthesisSensorNum(sensorIndex)))
					.headers(header)
					.check(status.is(200)))
			//check that the synthesis is correct
			.exec(session => {
					//test the synthesis against the stored values 
						if((session(SynthesisSensorNum(sensorIndex)).validate[Vector[Any]].get(getSynthesisResultIndex(sensorIndex,"minValue")))==partialminValue(sensorIndex)
							&& session(SynthesisSensorNum(sensorIndex)).validate[Vector[Any]].get(getSynthesisResultIndex(sensorIndex,"maxValue"))==partialMaxValue(sensorIndex)
							&& session(SynthesisSensorNum(sensorIndex)).validate[Vector[Any]].get(getSynthesisResultIndex(sensorIndex,"mediumValue"))==(partialSumValue(sensorIndex)/msgPackage)){
					
					//call counter max
					 counterMax(sensorIndex)=0
					//call counter min
					 counterMin(sensorIndex)=0
					 //initialise la somme
					partialSumValue(sensorIndex)=0
					}else{
						//exit if results are not valid
						System.exit(1)
					}

					session
					})
  	}


  	//number of packages sent by a injector
	var numOfPackages=10

	//build 10 scenarios that represent the 10 injectors 
	val scenariosBuild=Array(

  		scenario("injecteur 1").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(0))},

  		scenario("injecteur 2").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(1))},

  		scenario("injecteur 3").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(2))},

  		scenario("injecteur 4").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(3))},

		scenario("injecteur 5").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(4))},

  		scenario("injecteur 6").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(5))},

        scenario("injecteur 7").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(6))},

        scenario("injecteur 8").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(7))},

        scenario("injecteur 9").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(8))},

        scenario("injecteur 10").repeat(numOfPackages){exec(ScenarioBuilder.SendMsgsAndCHeckSynthesis(9))}
  		)

	/**we run the scenarios and assert that 100% 
  	*of messages were received
  	*/

 	setUp(scenariosBuild(0).inject(atOnceUsers(1)),
	  	scenariosBuild(1).inject(atOnceUsers(1)),
	  	scenariosBuild(2).inject(atOnceUsers(1)),
	  	scenariosBuild(3).inject(atOnceUsers(1)),
	  	scenariosBuild(4).inject(atOnceUsers(1)),
	  	scenariosBuild(5).inject(atOnceUsers(1)),
	  	scenariosBuild(6).inject(atOnceUsers(1)),
	  	scenariosBuild(7).inject(atOnceUsers(1)),
	  	scenariosBuild(8).inject(atOnceUsers(1)),
	  	scenariosBuild(9).inject(atOnceUsers(1)))
	  .protocols(httpProtocol)
	  .assertions(global.successfulRequests.percent.is(100))

	
	 	/** This Bloc of code runs after the simulation
  	* the end time of the simulation 
  	*it verifies that the total  synthesis is correct
  	* and sends the results to the leaderBoard
  	*/

  	after{

  		//end Time
  		simulationEndTime=System.nanoTime()

  		println("la simulation est finie traitement en cours...")

  		//this function retreives the values of a map made from a json object
   		def show(x: Option[Any],sensorTypeIndex:Int,synthesisValue:String) = x match {
  			
      			case Some(m: List[Map[String, Any]]) => (m(sensorTypeIndex))(synthesisValue) match{
      				case s: String => s
      				case i: Int => i
      				case d: Double => d
      			}

      			case None =>
   		}
  		
  		//total time of the simulation in nanoseconds
  		val timeOfSimulation=simulationEndTime-simulationStartTime

  		println("Temp d'execution:"+timeOfSimulation+" Equipe:"+teamName+" rattachement:"+teameLocation)
		//the results checker
		var resultatValid=true
		
		//this is the url of the synthesis get method that sends a synthesis object containing 10 sensor types results
  		val urlSyhtesis =	"http://192.168.1.1/messages/synthesis?timestamp="
  						+java.net.URLEncoder.encode(formatter.format(simulationStartTime), "utf-8")
  						+"&duraion="+((timeOfSimulation/1000000000)+1).toInt
  						
  		val result = scala.io.Source.fromURL(urlSyhtesis)

  		//setting the json parser to return integers and doubles 
  		scala.util.parsing.json.JSON.globalNumberParser =  {
  		in =>
    			try in.toInt catch { case _: NumberFormatException => in.toDouble}
		}

  		val SynthesisJson = scala.util.parsing.json.JSON.parseFull(result.mkString)

  		
		
  		
  		for( a <- 0 to 9){

	 		if(show(SynthesisJson,a,"minValue")==totalMinValues(a) && 
	 		   show(SynthesisJson,a,"maxValue")==totalMaxValues(a) && 
	 		   show(SynthesisJson,a,"mediumValue")==(totalSumValues(a)/(msgPackage*numOfPackages)) ){

	 				println("les résultats du sensorType"+show(SynthesisJson,a,"sensorType")+"sont valides")
	 				
	 		}else{
	 				println("les résultats du sensorType"+show(SynthesisJson,a,"sensorType")+"ne sont pas valides!!!!")
	 				resultatValid=false

	 		}
	 		
  		}
  		
		//if the results are valid they are sent to the leaderBoard
		if(resultatValid==true){

			println("Temp d'execution:"+timeOfSimulation+" Equipe:"+teamName+"participant:"+teamMembers+" rattachement:"+teameLocation)

  			val password=scala.io.StdIn.readLine("entrez le mot de passe pour valider le résultat?: ")
        	 	
        	 	//the leader board is up
        	 	val urlLeaderBoard="http://concoursiot.northeurope.cloudapp.azure.com/results"

        	 	val post = new HttpPost(urlLeaderBoard)
        		val client = HttpClientBuilder.create().build()
        		
                	post.setEntity(new StringEntity("""{"teamName":""""+teamName+"""",
                					    "time":""""+timeOfSimulation+"""",
                					    "location":""""+teameLocation+"""",
                					    "teamMembers":""""+teamMembers+"""",
                					    "password":""""+password+""""}"""))

                	val response = client.execute(post)

        		  if(response.getStatusLine().getStatusCode()==200){
                			println("votre résultat est envoyé, n'oubliez pas de regarder le leaderBoard pour voir votre classement...")
        		 }else{
                			println("L'envoi a échoué!!! le résultat n'a pas été envoyé au leaderboard!!!")
        		 }

		}else{
			println("votre synthése n'est pas valide, vous ne pouvez pas envoyer vos résultats!")
			System.exit(1)
		}
	 
  	}
  }
