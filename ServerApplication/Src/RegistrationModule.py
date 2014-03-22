'''
Created on 11-Mar-2014

@author: nishant
'''
import json
import threading

class RegistrationProcessor(threading.Thread):
    
    def __init__(self, rMessage):
        self.rMessage = rMessage
        
        
        '''This is the method called when this thread is started.'''
    def run(self):
        ''' Check if the message is registration or deregistration '''
        self.msgObject = json.loads(self.rMessage['body'])
        if self.msgObject['operation'] in ('registration'): #preliminary Check, might change later
            self.registerUser(self.msgObject)
        else:
            self.deRegisterUser(self.msgObject)
        return
    
    def registerUser(self, msgObject): #(long userID,long password, string[] sensorsPresent): #msg contains userid+capabilities 
        ''' To do:
            Parse the incoming message and store the capabilities of a particular user in the DB
        '''
        
        
        return 
         
    def deRegisterUser(self, msgObject): #(long userID):    
        
        ''' To do:
                Remove the entries for the userID from the DB
                Remember to deregister from the jabber server as well apart from this. 
            '''
        return
    
def processRegistrationMessage(rMessage):
    processor = RegistrationProcessor(rMessage)
    processor.start()
