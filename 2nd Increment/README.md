# 2nd Increment

##Description:

###Mobile Phone to Spark Connection:
Here we have created an android application which will send the data stream to spark by creating a socket stream connection. We have implemented speech to Text implementation and sending that text to Spark Server.
###Sending data to Spark:
Here the data is sent to Spark server by creating a socket streaming connection with spark and sending data over to Spark.
###Sentiment Analysis on Spark:
Here once the data was sent to Spark server, that data will be analyzed using Stanford NLP and predicted whether the sent data is positive or negative scenario related. 
###Getting Response back to Phone from Spark:
Based on the information analyzed by Stanford NLP the analyzed data will be sent back to phone where the android phone will do further analysis on that and will perform particular set of operations based on output. For example if output is positive, then robot/android phone will ask “You seem to be in happy/nice mood. How are you doing today?” If the output is negative, then the robot/android phone will ask “You seem so sad/feeling down today. Feel better soon. Can I play a song for you or tell a joke”