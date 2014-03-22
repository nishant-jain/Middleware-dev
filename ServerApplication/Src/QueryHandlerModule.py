'''
Created on 11-Mar-2014

@author: nishant
'''
import threading
import json

class QueryProcessor(threading.Thread):
    def __init__(self, qMessage):
        super(QueryProcessor, self).__init__()
        self.qMessage = qMessage
        self.queryObject = ""
        
    ''' This is the method that runs on starting this thread. '''
    def run(self):
        self.queryObject = json.loads(self.qMessage['body']) #This object now holds the Python Dictionary Object of the JSON query
        
    def fetchParameters(self, JSONquery):
        ''' To do:
        Parse parameters of query from the received msg and store it in DB
        returns an object of Query class containing attribute values specified in the JSON query message. 
        '''
            
        return 
    
    def lookup(self, query):
        ''' To do:
        returns true if the query can be serviced by already available queries in the Query Database
        ; otherwise returns false and the query is forwarded to the DataRequester Module.
        '''
        return 
    
    def sendAcknowledgement(self): 
        ''' To do:
        sends an acknowledgement to the publisher stating that the query has been received
         and he will be notified when sufficient subscribers will provide the information.
        '''
        return 
    
    def sendConfirmation(self):
        ''' To do:
        send a message to the requester informing whether the query will be serviced.
        '''
        return 
        


def queryparse(msg): #parse queries
    #print msg['body']
    processor = QueryProcessor(msg)
    processor.start()

