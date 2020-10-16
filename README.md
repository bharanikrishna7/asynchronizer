# ASYNCHRONIZER
CircleCI Status : [![CircleCI](https://circleci.com/gh/bharanikrishna7/asynchronizer/tree/master.svg?style=svg)](https://circleci.com/gh/bharanikrishna7/asynchronizer/tree/master)

Coverage Report: [![codecov](https://codecov.io/gh/bharanikrishna7/asynchronizer/branch/master/graph/badge.svg)](https://codecov.io/gh/bharanikrishna7/asynchronizer)

## INTRODUCTION
This project aims to simplify multi-threading in Scala (also to some extent Java).


This project will provide 2 APIs:
1. AsynchronousTask
2. AsynchronousCoordinator

## STRUCTURE
### Asynchronous Task
API to execute a future Task with better debugging and option to cancel* the task.
Current abilities:
* superfine debugging (thanks to state design pattern).
    * ability to capture time spent executing a task (even when it fails).
    * states help in relatively easy visualization when trying to understand what state a task is in.
* ability to cancel task.
* ability to wait set amount of time and try to retrieve results.
* unsafe asynchronous cancellation (useful for debugging / haven't found any suitable use case for production scenario). 

### Asynchronous Coordinator [TO-DO]
API to execute bunch of Asynchronous Tasks with abilities like: 
* partial execution results
* return both results and exceptions.
* Debugging abilities (state and other info associated with queued asynchronous threads)  
