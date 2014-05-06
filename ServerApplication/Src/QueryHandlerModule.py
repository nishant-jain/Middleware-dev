'''
Created on 11-Mar-2014

@author: nishant

'''
import threading
import json
from Models import Query
from Models import SensorUserRel,Sensor,User
import Queue
import datetime

'''Constants!'''
PROVIDER_REQUEST_TIMEOUT = 60.0 #60 seconds!

class QueryProcessor(threading.Thread):
    def __init__(self, msgHandler, qMessage):
        super(QueryProcessor, self).__init__()
        self.qMessage = qMessage
        self.queryObject = ""
        self.msgHandler = msgHandler
        self.q = Queue.Queue()
        self.timeout = 1.0/60.0
        self.amIDone = False
        self.queryNo = ''
        self.currentCount = 0
        self.usersServicing = []
        self.finalDataPackage = {}
        self.finalDataPackage['noOfFiles'] = 0
        self.hostName = str(self.qMessage['from']).split("@")[1]
        self.waiting = False
        self.receivedCount = 0
        
    
    def onThread(self, function, *args, **kwargs):
        self.q.put((function, args, kwargs))
        
    def providerRequestTimeout(self):
        if(self.currentCount < eval(str(self.queryObject['countMin']))):
            #We have timed out and haven't received enough providers yet. We should regrettably inform the requester and close the transaction.
            self.sendFinalConfirmation(False)
            ''' Time to die! '''
            self.amIDone = True
            return

        
    def putProviderRequestTimeoutOnThread(self):
        self.onThread(self.providerRequestTimeout)
        
        
    def dataCollectionTimeout(self):
        if(self.receivedCount < self.currentCount):
            #We have timed out and haven't received enough data responses yet. Unfortunate ending to our query.
            #Just die for now, but 
            ''' TODO: Send a sorry message to the client! '''
            print 'Timed out on expiry time but not enough data responses. Dying now!'
            self.amIDone = True
            
            return

        
    def putDataCollectionTimeoutOnThread(self):
        self.onThread(self.dataCollectionTimeout)

        
    def processMessage(self, msg):
        ''' The msg object is actually the raw XMPP message object. Parse it yourself!'''
        newMsg = json.loads(msg['body'])
        if str(msg['subject'])=='ProviderResponse':
            ''' This is a message which gives the response of some provider which we flooded for query results! '''
            if(str(newMsg['status'])=='Accepted'):
                #Party! Request accepted!
                    
                u = User.get(User.username==str(msg['from']).split("@")[0])
                
                if(self.currentCount >= eval(str(self.queryObject['countMin']))):
                    #We don't need this guy anymore. Send him a not required message.
                    self.sendProviderConfirmation(u, False)
                    return
                
                self.usersServicing += [u]
                self.currentCount += 1
                
                self.sendProviderConfirmation(u, True)
                
                if(self.currentCount >= eval(str(self.queryObject['countMin']))):
                    #Send final confirmation to client!
                    self.waiting = True
                    self.sendFinalConfirmation(True)
                    
                    ''' Set Timeout for query expiry time! '''
        
                    #Check for bad query expiry times! 
                    if self.queryDBObject.expiryTime < datetime.datetime.now():
                        '''Bad time. Set query expiry time to be toTime-now() + 60 seconds'''
                        toSet = (self.queryDBObject.toTime - datetime.datetime.now()).total_seconds() + 60
                        if(toSet <= 20):
                            #Something is really bad. Just set expiry time out to 4 minutes and pray its all good.
                            toSet = 240
                    else:
                        toSet = (self.queryDBObject.expiryTime - datetime.datetime.now()).total_seconds() + 60
                        if(toSet <= 20):
                            #Something is really bad. Just set expiry time out to 4 minutes and pray its all good.
                            toSet = 240
                            
                    ''' Set expiry timeout'''
                    threading.Timer(toSet, self.putDataCollectionTimeoutOnThread)
                    
            else:
                #Snobby client, rejected our request. Ignore this guy!
                pass
        elif str(msg['subject'])=='Data':
            ''' This is a message which somehow provides the final data given by the provider! We're nearly done now.'''
            ''' UPDATE - The data provided is in the form of a JSON message. Parse it and store it in our response message! '''
            curCount = self.finalDataPackage['noOfFiles']
            curCount += 1
            self.finalDataPackage['sensorData' + str(curCount)] = newMsg['sensorData']
            self.finalDataPackage['noOfFiles'] = curCount
            self.receivedCount += 1
            if(curCount >= self.currentCount):
                'We are done now! Send the data back to the requester and be done with life now!'
                msgToSend = str(json.dumps(self.finalDataPackage))
                self.msgHandler.send_message(mto=self.qMessage['from'], mbody=msgToSend, msubject='RequestedData')
                
                ''' Time to die! '''
                self.amIDone = True
                pass
        else:
            print 'Got unrecognized message for queryNo: ' + str(self.queryNo) + '. Message subject: ' + str(msg['subject'])
            
            
    ''' This is the method that runs on starting this thread. 
    EDIT - It is now more of an event loop, listening for more messages and stuff to do for this query
    until it is finished. Ugly but should work for now.'''
    def run(self):
        self.queryObject = json.loads(self.qMessage['body']) #This object now holds the Python Dictionary Object of the JSON query
        self.queryNo = str(self.queryObject['queryNo'])
        query_possible = self.storeQueryInDB(self.queryObject)
        if(query_possible):
            self.floodProviders()
            ''' Start a timer to check for enough providers after timeout! '''
            threading.Timer(PROVIDER_REQUEST_TIMEOUT, self.putProviderRequestTimeoutOnThread).start()
            print 'Providers flooded and timeout set. queryNo: ' + self.queryNo
            self.finalDataPackage['sensorType'] = str(self.queryObject['dataReqd'])
            self.finalDataPackage['queryNo'] = self.queryNo
        else:
            print 'Could not satisfy query no: ' + str(self.queryNo) + '! Aborting...'
            self.amIDone = True
            return 
        
        ''' we have flooded providers now. Should start listening for messages! '''
       
        
        while (self.amIDone==False):
            try:
                function, args, kwargs = self.q.get(timeout=self.timeout)
                function(*args, **kwargs)
            except Queue.Empty:
                pass
            
        print 'Thread for queryNo: ' + str(self.queryNo) + ' now dying...'
    
    def sendProviderConfirmation(self, user, confirmed):
        if(confirmed):
            toSend = '{"queryNo":"' + str(self.queryNo) + '", "finalStatus":"Confirmed"}'
        else:
            toSend = '{"queryNo":"' + str(self.queryNo) + '", "finalStatus":"Rejected", "errorMessage": "Already got the required providers! :)"}'
        self.msgHandler.send_message(mto=(str(user.username) + "@" + self.hostName), mbody=toSend, msubject='Final Confirmation')
        print "Provider Confirmation sent for query no: " + str(self.queryNo) + ". Status: " + str(confirmed) + " to User: " + str(user.username)
       
    
    def sendFinalConfirmation(self, confirmed):
        if(confirmed):
            toSend = '{"queryNo":"' + str(self.queryNo) + '", "finalStatus":"Confirmed"}'
        else:
            toSend = '{"queryNo":"' + str(self.queryNo) + '", "finalStatus":"Rejected", "errorMessage": "Not enough providers currently!"}'
        self.msgHandler.send_message(mto=self.qMessage['from'], mbody=toSend, msubject='Final Confirmation')
        print "Requester Confirmation sent for query no: " + str(self.queryNo) + ". Status: " + str(confirmed)
        return
    
    def floodProviders(self):
        u=Sensor.select().where((str(self.queryObject['dataReqd'])== Sensor.SensorType)&((Sensor.minDelay==0)|(eval(str(self.queryObject['frequency']))<1000000.0/Sensor.minDelay)) ).distinct()
        z = User.select().join(SensorUserRel).where(SensorUserRel.sensor << u).distinct()
        ''' Now z has all the users we have to send a request to! '''
        
        toSend = '{"queryNo":"' + str(self.queryNo) + '","sensorType":"' + str(self.queryObject['dataReqd']) + '","frequency":"' + str(self.queryObject['frequency']) + '",'
        toSend = toSend + '"Activity":"' + str(self.queryObject['activity']) + '", "fromTime":"' + str(self.queryObject['fromTime']) + '", "toTime":"' + str(self.queryObject['toTime']) + '"}'
         
        #self.msgHandler.send_message(self.qMessage['from'], toSend)
        serverAppend = '@' + self.hostName
        
        for i in z:
            self.msgHandler.send_message(mto=(str(i.username) + serverAppend), mbody=toSend, msubject="DataRequest")

        ''' Query requests sent out; should now start listening for replies! '''
        
    def storeQueryInDB(self, qObj):
        ''' To do:
        Parse parameters of query from the received msg and store it in DB
        '''
        foundFlag = False
        try:
            uname = User.get(User.username==str(self.qMessage['from']).split("@")[0])
            foundFlag = True
        except User.DoesNotExist:
            print "User not in DB"
            return False
        
        qResults = Query.select().where(Query.queryNo==str(qObj['queryNo'])).count()
        if(qResults!=0):
            print 'Discarding message for queryNo: ' + str(qObj['queryNo']) + ' as it already existed in DB'
            return False
        
        if foundFlag:
            q = Query()
            q.username = uname
            q.queryNo = str(qObj['queryNo'])
            q.dataReqd = str(qObj['dataReqd'])
            q.frequency = eval(str(qObj['frequency']))
            q.Latitude = eval(str(qObj['latitude']))
            q.Longitude = eval(str(qObj['longitude']))
            q.fromTime = datetime.datetime.fromtimestamp(eval(str(qObj['fromTime']))/1000)
            q.toTime = datetime.datetime.fromtimestamp(eval(str(qObj['toTime']))/1000)
            q.expiryTime = datetime.datetime.fromtimestamp(eval(str(qObj['expiryTime']))/1000)
            q.Location = 'hardcoded'#str(qObj['location'])
            q.Activity = str(qObj['activity'])
            q.countMin = eval(str(qObj['countMin']))
            q.countMax = eval(str(qObj['countMax']))
            q.countReceived = 0
            
            q.save()    
            self.queryDBObject = q
                   
            if(self.queryPossible()):
                self.sendAcknowledgement(True)
                return True
            else:
                self.sendAcknowledgement(False)
                return False
    
    def queryPossible(self):
        '''Check from the database if we even have the requested number of devices to service the query.'''
        #if found return True
        #To Write a SELECT query when DB Schema finalized.
        #assuming no future queries for now
        #Untested code
        u=Sensor.select().where((str(self.queryObject['dataReqd'])== Sensor.SensorType)&((Sensor.minDelay==0)|(eval(str(self.queryObject['frequency']))<1000000.0/Sensor.minDelay)) ).distinct()
        count=0
        for p in u:
                count=count+p.users.count()
        #count=User.select().join(Sensor).join(SensorUserRel).where(SensorUserRel.user == SensorUserRel.sensor).count()
        
        ''' Possible query to get all users of such sensors in u:
         z = User.select().join(SensorUserRel).where(SensorUserRel.sensor << u).distinct()
         '''
            
        print 'Found count=' + str(count) + ' for query Number: ' + self.queryNo
        
        if(count>=eval(str(self.queryObject['countMin']))):
            #Query is possible; initiate messages to valid subscribers to respond with an acknowledgement.            
            return True
        else:
            return False
    
    def lookup(self, query):
        ''' To do:
        returns true if the query can be serviced by already available queries in the Query Database
        ; otherwise returns false and the query is forwarded to the DataRequester Module.
        '''
        
        return 
    
    def sendAcknowledgement(self, accepted=True, errMessage=''): 
        toSend = '{"queryAck":'
        if(accepted):
            toSend = toSend + '"accepted"'
        else:
            toSend = toSend + '"denied"'
            
        toSend = toSend + ', "errMessage": "' + errMessage
        toSend = toSend + '", "queryNo":"' + str(self.queryNo) + '"}'
        self.msgHandler.send_message(mto=self.qMessage['from'], mbody=toSend, msubject='isQueryPossible')
        
        return 
    
    def sendConfirmation(self):
        ''' To do:
        send a message to the requester informing whether the query will be serviced.
        '''
        return 
        


def queryparse(msgHandler, msg): #parse queries
    #print msg['body']
	print 'Got query'
	qno = str(json.loads(msg['body'])['queryNo'])
	print 'Got message from query number: ' + qno
	for i in threading.enumerate():
		if i.name==qno:
			print 'Found a thread already for query number: ' + qno
			i.onThread(QueryProcessor.processMessage, i, msg)
			return
	'''TODO :need to check for msgs related to threads/queries which have been already terminated'''
	#else, if no current object found, create a new one!
	print "Creating new Query thread."
	processor = QueryProcessor(msgHandler, msg)
	processor.name = qno
	processor.start()


