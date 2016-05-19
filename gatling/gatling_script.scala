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
import scala.reflect.runtime.universe._
/** 
  *This class does the test by spraying a msgPackage x numOfPackages x 10 of http post requests to your service (/messages) Concurrently
  * verfies that your synthesis service works given a timestamp and a duration
  * verifies that your synthesis for the total of values sent  is correct, 
  * calculates how much time the simulation took
  * sends the results to the leaderBoard (validate if you have the password)
  */

  class InjectionsAndVerificationsTest extends Simulation {

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
	//the simulation end in seconds
	var simulationStartTimeMs=0.0

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
	var totalSumValues = Array[scala.math.BigDecimal](
		BigDecimal(0),BigDecimal(0),BigDecimal(0),
		BigDecimal(0),BigDecimal(0),BigDecimal(0),BigDecimal(0),
		BigDecimal(0),BigDecimal(0),BigDecimal(0))
	


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
		totalSumValues(sensorTypeIndex) = totalSumValues(sensorTypeIndex) + BigDecimal(value)

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
	var partialSumValue = Array[scala.math.BigDecimal](
		BigDecimal(0),BigDecimal(0),BigDecimal(0),
		BigDecimal(0),BigDecimal(0),BigDecimal(0),BigDecimal(0),
		BigDecimal(0),BigDecimal(0),BigDecimal(0))
	/**
	 *generates the json message (including the random number)
	 *save the minimum/the maximum and the sum 
	 *@param  the Index of the sensorType 
	 *@return a json formated string that will be the body of the message
	 */ 
	///json generator
	def generateJson(sensorIndex:Int):String={
		val formatter2  = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ") 
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));

		var timeNow=Calendar.getInstance().getTimeInMillis()
		var number=generateNum(sensorIndex)

		var jsonMsg="""{"id":""""+generateId()+"""",
						"timestamp":""""+formatter2.format(timeNow)+"""",
						"sensorType":"""+(sensorIndex+1)+""",
						"value":"""+number+""" }"""

		partialMaxValue(sensorIndex)=maxNum(number,sensorIndex)
		partialminValue(sensorIndex)=minNum(number,sensorIndex)
		partialSumValue(sensorIndex)=partialSumValue(sensorIndex)+BigDecimal(number)

		return jsonMsg

	}


	/** This Bloc of code runs before the simulation
  	* it retreives the start time of the simulation 
  	*/
	before {
    		println("la simulation est sur le point de commencer...")
    		simulationStartTimeMs=Calendar.getInstance().getTimeInMillis()
    		simulationStartTime=System.nanoTime()		
  	}


  	//the number of messages in a  package
	var msgPackage = 1000
	//the synthesis duration of a package of tests in miliseconds
	var duration=Array(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
	//this function retreives the values of a map made from a json object
   		def show(x: Option[Any],sensorTypeIndex:Int,synthesisValue:String) = x match {
  			
      			case Some(m: List[Map[String, Any]]) => (m(sensorTypeIndex))(synthesisValue) match{
      				case s: String => s
      				case i: Int => i
      				case d: Double => d
      			}

      			case None =>
   		}	

   		def size(x: Option[Any]) = x match {
  			
      			case Some(m: List[Map[String, Any]]) => m.size

      			case None =>
   		}
	/**
	 *this object is the scenario builder 
	 * a scenarios sends a msgPackage for all type sensors ...
	 */ 
	object ScenarioBuilder {
  		
  		def SynthesisSensorNum(sensorIndex:Int):String={
  			return "SynthesisSensor"+sensorIndex.toString()
  		}
  		//constructs the request name depending on the injector
  		def requestName(sensorIndex:Int):String={
  			return "request_"+sensorIndex.toString()
  		}
  	

  		//setting the json parser to return integers and doubles 
  		scala.util.parsing.json.JSON.globalNumberParser =  {
  		in =>
    			try in.toInt catch { case _: NumberFormatException => in.toDouble}
		}

  		/**
	 	 *sends a package of messages and checks if the synthesis of the package is correct
		 *@param  the Index of the sensorType
		 *@return a scenario that sends a package of messages containing msgPackage + checks the synthesis 
		 */ 

  		def SendMsgspackage(sensorIndex:Int)= exec(session=>{
  			//time the package of messages started sending...
			startTimePackage(sensorIndex)=Calendar.getInstance().getTimeInMillis()
			session

			})
  			//send a package of messages to all sensors and pause to round the time to seconds
  				.repeat(msgPackage){  
				exec(http(session=>requestName(sensorIndex+1))
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
			val prdr= "paramDuration"+(sensorIndex+1).toString()
			val prtm="paramTimestamp"+(sensorIndex+1).toString()
			//Put parameters in session for next synthesis request
			session.set(prdr, ""+(duration(sensorIndex)/1000).toInt).set(prtm, new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSZ").format(startTimePackage(sensorIndex)))
			})
			
			def scenariosVerifier(sensorIndex:Int)=exec(
  					repeat(numOfPackages){
					exec(ScenarioBuilder.SendMsgspackage(sensorIndex))
					.exec(http("Sensor_Synthesis_Check")
					.get("http://192.168.1.1/messages/synthesis")
					.queryParam("duration", session=>session("paramDuration"+(sensorIndex+1).toString()).validate[String])
					.queryParam("timestamp", session=>session("paramTimestamp"+(sensorIndex+1).toString()).validate[String])
  					.check(jsonPath("$..*").findAll.saveAs(SynthesisSensorNum(sensorIndex+1)))
					.headers(header)
					.check(status.is(200)))
			//check that the synthesis is correct
			.exec(session => {	
						val Synthesisobj = scala.util.parsing.json.JSON.parseFull(session(SynthesisSensorNum(sensorIndex+1)).validate[Vector[String]].get(0))
						println(Synthesisobj)
						for( a <- 0 to size(Synthesisobj).asInstanceOf[Int]-1){
							if((show(Synthesisobj,a,"sensorType").asInstanceOf[Int]==sensorIndex+1)){
								if(show(Synthesisobj,a,"minValue").asInstanceOf[Int]==partialminValue((show(Synthesisobj,a,"sensorType")).asInstanceOf[Int]-1)
								 && show(Synthesisobj,a,"maxValue").asInstanceOf[Int]==partialMaxValue((show(Synthesisobj,a,"sensorType")).asInstanceOf[Int]-1)
							 	&& show(Synthesisobj,a,"mediumValue").asInstanceOf[Double]==(partialSumValue((show(Synthesisobj,a,"sensorType")).asInstanceOf[Int]-1)/msgPackage).setScale(2, BigDecimal.RoundingMode.HALF_UP)
							 	){
								
								println("les résultats sont valides"+(show(Synthesisobj,a,"sensorType")))
								//call counter max
 								counterMax(sensorIndex)=0
 								//call counter min
 								counterMin(sensorIndex)=0
 								//initialise la somme
 								partialSumValue(sensorIndex)=BigDecimal(0)

								}else{

									println("les resultats sont invalides!!")
									println((show(Synthesisobj,a,"sensorType")))
									println(partialminValue((show(Synthesisobj,a,"sensorType")).asInstanceOf[Int]-1))
									//System.exit(1)
								}
							}
						
						
						}
						
						

					session
					})
		})
			
  	}


  	//number of packages sent by a injector
	var numOfPackages=100

//build 10 scenarios that represent the 10 injectors 
	val scenariosBuild=Array(

  		scenario("injecteur 1").exec(ScenarioBuilder.scenariosVerifier(0)),

  		scenario("injecteur 2").exec(ScenarioBuilder.scenariosVerifier(1)),

  		scenario("injecteur 3").exec(ScenarioBuilder.scenariosVerifier(2)),

  		scenario("injecteur 4").exec(ScenarioBuilder.scenariosVerifier(3)),

		scenario("injecteur 5").exec(ScenarioBuilder.scenariosVerifier(4)),

  		scenario("injecteur 6").exec(ScenarioBuilder.scenariosVerifier(5)),

        scenario("injecteur 7").exec(ScenarioBuilder.scenariosVerifier(6)),

        scenario("injecteur 8").exec(ScenarioBuilder.scenariosVerifier(7)),

        scenario("injecteur 9").exec(ScenarioBuilder.scenariosVerifier(8)),

        scenario("injecteur 10").exec(ScenarioBuilder.scenariosVerifier(9))
  		) 

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

  		
  		
  		//total time of the simulation in nanoseconds
  		val timeOfSimulation=simulationEndTime-simulationStartTime

  		println("Temp d'execution:"+timeOfSimulation+" Equipe:"+teamName+" rattachement:"+teameLocation)
		//the results checker
		var resultatValid=true
		
		//this is the url of the synthesis get method that sends a synthesis object containing 10 sensor types results
  		val urlSyhtesis = "http://192.168.1.1/messages/synthesis?timestamp="
  						.concat(java.net.URLEncoder.encode(formatter.format(simulationStartTimeMs), "utf-8"))
  						.concat("&duration=")
  						.concat(""+((timeOfSimulation/1000000000)+1).toInt)
  						
  		val result = scala.io.Source.fromURL(urlSyhtesis)

  		

  		val SynthesisJson = scala.util.parsing.json.JSON.parseFull(result.mkString)

  		

  		for( a <- 0 to size(SynthesisJson).asInstanceOf[Int]-1){
						if(show(SynthesisJson,a,"minValue").asInstanceOf[Int]==/*123*/totalMinValues((show(SynthesisJson,a,"sensorType")).asInstanceOf[Int]-1)
							 && show(SynthesisJson,a,"maxValue").asInstanceOf[Int]==/*123*/totalMaxValues((show(SynthesisJson,a,"sensorType")).asInstanceOf[Int]-1)
							 && show(SynthesisJson,a,"mediumValue").asInstanceOf[Double]==/*123.11*/(totalSumValues((show(SynthesisJson,a,"sensorType")).asInstanceOf[Int]-1)/(numOfPackages*msgPackage)).setScale(2, BigDecimal.RoundingMode.HALF_UP)
						){

								println("les résultats sont valides")

								}else{
									println("les resultats sont invalides!!")
									resultatValid=false
									System.exit(1)
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
