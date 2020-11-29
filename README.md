# JobCoinMixerJobCoinMixer is a RESTful Web Servicethat that accept HTTP POST requests at http://localhost:8080/mixer/api/mix
It expects the following JSON in the body:
{"withdrawalAddresses":["<addr1>","<addr1>","<addr1>"]}
where <addrx> is a place holder for the actual withdrawal address

If successfull web service would return deposit address 
sample success response JSON :
{
    "errorCode": 0,
    "errorDescription": null,
    "depositAddress": "cZIWgT2ZmYtVTtt8YqqBVNpBLBrJ5p0Jn7HWI8yw"
}

Deposit adress would remain on the watch list until funds are received or 24 hours passed since request was posted

Prerequisites:
Java 11 or higher.
Maven 3.6.0. or higher

1.  How to build: 
    Clone the github repository into disk folder of your choice from https://github.com/czemelman/JobCoinMixer.git
    From the folder where you cloned the repository change direcory to \JobCoinMixer\JobCoinMixer folder
    Once inside \JobCoinMixer directory execute the following command: mvn clean install 
   
2.  How to start the app:
    Once build outlined in step 1 is succcesfully finished from the same folder \JobCoinMixer\JobCoinMixer
    execute the following command:
    mvn spring-boot:run

3.  How to run all unit tests:
    From the same folder as you used to build the app JobCoinMixer run the following command mvn test

4.  Runnig just unit test that demonstrates the following:
    Sends request to mix with 3 different unused withdrawal addresses
    Reads a deposit address from  mix request and sends random jobcoin amount  to the deposit address specified
	Waits for 60 seconds (pre configured max deposit delay time) 
	Aggregate balance from all the withdrawal addresses
    Asserts that aggregated cumulative balance is equal to deposit amount minus fee collected (pre configured fee is 5%
    
    From the same folder as you used to build the app JobCoinMixer run the following command mvn -Dtest=MainMixerTest test
