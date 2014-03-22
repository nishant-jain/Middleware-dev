'''
Created on 11-Mar-2014

@author: nishant
'''
import threading
import json
from Models import Query

class QueryProcessor(threading.Thread):
    def __init__(self, msgHandler, qMessage):
        super(QueryProcessor, self).__init__()
        self.qMessage = qMessage
        self.queryObject = ""
        self.msgHandler = msgHandler
        
    ''' This is the method that runs on starting this thread. '''
    def run(self):
        self.queryObject = json.loads(self.qMessage['body']) #This object now holds the Python Dictionary Object of the JSON query
        self.storeQueryInDB(self.queryObject)
        
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
        q.countReceved = 0
        
        q.save()    
           
        if(self.queryPossible()):
            self.sendAcknowledgement(True)
        else:
            self.sendAcknowledgement(False)
            
            
            
        return 
    
    def queryPossible(self):
        '''Check from the database if we even have the requested number of devices to service the query.'''
        #if found return True
        #To Write a SELECT query when DB Schema finalized.
        
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
    processor = QueryProcessor(msgHandler, msg)
    processor.start()

