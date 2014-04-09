'''
Created on Mar 22, 2014

@author: Kshitiz

This module is used to 1) check the willingness of capable devices to service query.
                        2)selecting the required number of collectors from the willing and capable device queue.
                        3)Sending a confirmation to those devices to start recording
                        Tentative : 4)Receive the URLs of those recorded files, which have been uploaded to the server
'''
import threading

class DataRequesterModule(threading.Thread):
    def __init__(self, queryObject,msgHandler):
        super(DataRequesterModule, self).__init__()
        self.queryObject = queryObject
        self.msgHandler= msgHandler     
        self.deviceQueue = "" #a device queue which holds UIDs of willing and capable devices
    ''' This is the method that runs on starting this thread. '''
    def run(self):
        self.sendQueryDetails()
        
        
    def sendQueryDetails(self):
        '''#4 to #6 in the design doc. Use self.queryObject to parse the query object available in json here.
        This function sends queries to all capable devices and sees if they are willing to participate'''
        '''Call sendConfirmation() from Class QueryHandlerModule if enough willing participants reply
        or make a new confirmation function here'''
        #Store UIDs in deviceQueue
        self.requestData()
        return
    
    def requestData(self):
        '''Requests data from those willing devices which are available in deviceQueue after 
        choosing the top x number of participants, where x is the required number(queryObject['countMin']
            Send a confirmation to them to start recording data.'''
        toSend=""#create toSend message. Todo: Decide the message protocol
        for i in self.deviceQueue:
            print #self.msgHandler.send_message(i, toSend) #i being the UID of the receipient  
        
        return
        '''Wait for return messages, probably URLs of the files uploaded to the FTP server '''
def sendRequests(queryHandlerObject):
    processor = DataRequesterModule(queryHandlerObject.queryObject,queryHandlerObject.msgHandler)
    processor.start()
