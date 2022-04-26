ReplicationLib
===


# About
Replication for master slave.

# Purpose
This library is designed to replicate data between master and slave for MTBIT's core 
    
    
# Transports
Network current support TCP only.  


# Java Version
This library will require Java 8

# Test cases

## Master and Slave launch at initial. Queue is at initial (no event in queue)

### Master launches first, slave launches after master. 
	Slave sync events with master normal
	In this case, Master don't need to wait consistency state. 
		  
### Slave launches first, master launches after slave about more than 1 minutes. 
	Slave will become to master and can receive event from outside.
	When master ups, slave send sync request to master. Master has smaller index than slave so it send request replicate event to slave
	In this case, Master need to wait to reach consistency state before receiving events from outside. 
	When master is in consistency state, slave change to slave state and also can not receive event from out side
	  
## Master and Slave are running then slave is down
	When Slave is up, it will have index smaller than master. 
	Slave sends sync request to master with last index it received from master when it was down. 
	Master receives sync request, start to send replicated event to slave at above last slave index.
		
	In this case, Master don't need to wait consistency state. It both receives event from outside while sending replicated event to slave
## Master and Slave are running then master is down
Slave now is master and start receive event from outside and when master up it needs to wait consistency state to receive event from outside. 
When master is up, slave send sync request to master with index is last replicated index of slave.

We have 2 cases in this situation:

### Master has more event than slave when it dies
	Master sends event from last replicated index of slave to slave until master have no event available. These events were mark removed event.
	Slave only do process those events and does not do anything else. if event is removed event, slave will not publish it.
	When master has no event available to send to slave, master send sync request to slave with index is last replicated index of slave because slave can have write new events from this index.
	Slave check sync request from master then replicate event which is not removed event to master. Master process events from slave then send sync request again to slave but index is now last index of queue. 

### Master has same event with slave when it dies
	Master check its index same with last replicated index of slave but slave can write more event from this index so master need to send sync request to slave. 
	If slave has more event from last replicated index, slave send events to master. Master process those events then send sync request to slave.


	Both of cases we have same process below:
	When slave reaches to consistency state, it switch to slave state and stop receiving event from outside then send sync request to master.
	When Master receives sync request, if it reaches to consistency state, it start receiving events from outside
	
## Master and Slave are running then both are down, then start up  
	If master has more event than slave, Slave sync events with master from last slave index
	If slave has more event than master. Master request sync event with slave from last master index 

	Problem:
	When slave dies it is in master state and write more new events from outside, we will don't have last replicated index so when master is up, slave will send sync request at last index of slave.
	But when master dies it has more event than slave at that time so we will don't know which event is removed event and lost some new slave's event  which has index same with removed event.

	In this case, before all dies, results was apply so the best solution is delete all queue then start master first then start slave (all are with load snapshot).


	