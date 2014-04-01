'''
Created on 11-Mar-2014

@author: nishant

'''
import threading
import json
from Models import Query
from Models import SensorUserRel,Sensor,User
import Queue
import DataRequesterModule

class QueryProcessor(threading.Thread):
    def __init__(self, msgHandler, qMessage):
        super(QueryProcessor, self).__init__()
        self.qMessage = qMessage
        self.queryObject = ""
        self.msgHandler = msgHandler
        self.q = Queue()
        self.timeout = 1.0/60.0
        self.amIDone = False
        self.queryNo = 0
        
        
    
    def onThread(self, function, *args, **kwargs):
        self.q.put((function, args, kwargs))
        
    def processMessage(self, msg):
        ''' The msg object is actually the raw XMPP message object. Parse it yourself!'''
        newMsg = json.loads(msg['body'])
        msgType = str(newMsg['msgType'])
        if msgType=='ProviderResponse':
            ''' This is a message which gives the response of some provider which we flooded for query results! '''
            pass
        elif msgType=='ProviderData':
            ''' This is a message which somehow provides the final data given by the provider! We're nearly done now.'''
            pass
            
    ''' This is the method that runs on starting this thread. 
    EDIT - It is now more of an event loop, listening for more messages and stuff to do for this query
    until it is finished. Ugly but should work for now.'''
    def run(self):
        self.queryObject = json.loads(self.qMessage['body']) #This object now holds the Python Dictionary Object of the JSON query
        self.queryNo = eval(self.queryObject['queryNo'])
        self.storeQueryInDB(self.queryObject)
        
        while True:
            try:
                function, args, kwargs = self.q.get(timeout=self.timeout)
                function(*args, **kwargs)
            except Queue.Empty:
                if(self.amIDone):
                    break
        
    def storeQueryInDB(self, qObj):
        ''' To do:
        Parse parameters of query from the received msg and store it in DB
        '''
        q = Query()
        q.username = qObj['username']
        q.queryNo = eval(qObj['queryNo'])
        q.dataReqd = qObj['dataReqd']
        q.frequency = eval(qObj['frequency'])
        q.Latitude = eval(qObj['latitude'])
        q.Longitude = eval(qObj['longitude'])
#         q.fromtime = qObj['fromTime']
#         q.toTime = qObj['toTime']
#         q.expiryTime = qObj['expiryTime']
        q.countMin = eval(qObj['countMin'])
        q.countMax = eval(qObj['countMax'])
        q.countReceived = 0
        
        q.save()    
           
        if(self.queryPossible()):
            self.sendAcknowledgement(True)
            DataRequesterModule.sendRequests(self)
        else:
            self.sendAcknowledgement(False)
            
            
            
        return 
    
    def queryPossible(self):
        '''Check from the database if we even have the requested number of devices to service the query.'''
        #if found return True
        #To Write a SELECT query when DB Schema finalized.
        #assuming no future queries for now
        #Untested code
        u=Sensor.select().where((self.queryObject['dataReqd']== Sensor.SensorType)and(eval(self.queryObject['frequency'])<1000/Sensor.minDelay) )
        count=0
        for p in u:
                count=count+p.users.count()
        #count=User.select().join(Sensor).join(SensorUserRel).where(SensorUserRel.user == SensorUserRel.sensor).count()
        
        ''' Possible query to get all users of such sensors in u:
         z = User.select().join(SensorUserRel).where(SensorUserRel.sensor << u).distinct()
         '''
        
        if(count>=self.queryObject['countMin']):
            return True
        else:
            return False
    
    def lookup(self, query):
        ''' To do:
        returns true if the query can be serviced by already available queries in the Query Database
        ; otherwise returns false and the query is forwarded to the DataRequester Module.
        '''
        
        return 
    
    def sendAcknowledgement(self, accepted=True, errMessage=""): 
        toSend = '{"queryAck":'
        if(accepted):
            toSend = toSend + '"accepted"'
        else:
            toSend = toSend + '"denied"'
            
        toSend = toSend + ', "errMessage": "' + errMessage + '"}'
        self.msgHandler.send_message(self.qMessage['from'], toSend)
        
        return 
    
    def sendConfirmation(self):
        ''' To do:
        send a message to the requester informing whether the query will be serviced.
        '''
        return 
        


def queryparse(msgHandler, msg): #parse queries
    #print msg['body']
    qno = str(json.loads(msg['body'])['queryNo'])
    for i in threading.enumerate():
        if i.name==qno:
            i.onThread(QueryProcessor.processMessage, msg)
            return
        
    #else, if no current object found, create a new one!
    processor = QueryProcessor(msgHandler, msg)
    processor.name = qno
    processor.start()


