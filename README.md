# ASYNCHRONIZER
[![CircleCI](https://circleci.com/gh/bharanikrishna7/asynchronizer/tree/main.svg?style=shield)](https://circleci.com/gh/bharanikrishna7/asynchronizer/tree/main)
[![codecov](https://codecov.io/gh/bharanikrishna7/asynchronizer/branch/main/graph/badge.svg)](https://codecov.io/gh/bharanikrishna7/asynchronizer)

This project will provide 2 APIs:
1. AsynchronizerTask
2. Asynchronizer

## STRUCTURE
### AsynchronizerTask
API to execute a future Task with better debugging and option to cancel* the task.
Current abilities:
* superfine debugging (thanks to state design pattern).
    * ability to capture time spent executing a task (even when it fails).
    * states help in relatively easy visualization when trying to understand what state a task is in.
* ability to cancel task.
* ability to wait set amount of time and try to retrieve results.
* unsafe asynchronous cancellation (useful for debugging / haven't found any suitable use case for production scenario). 

### Asynchronizer
API to execute bunch of Asynchronous Tasks with abilities like: 
* partial execution results
* return both results and exceptions.
* Debugging abilities (state and other info associated with queued asynchronous threads)  
* Efficient memory usage (will smartly identify and terminate threads/tasks if not required to execute. Interrupt function will be called in threads which are not required).

## CHANGELOG
#### BUILD - 0.4.0
* Better implementation of task watchers (executed, passed, failed task count). Now watchers in asynchronizers are updated at the end of every task (completion / failure). This prevents a bug which previously caused sometimes ready variable to be not marked appropriately or state being incorrect.
* Updated unit tests to cover scenario where errors occur at both start and end of the list of tasks while middle tasks run successfully.
* Added CircleCI for Continuous Integration and Codecov for Coverage reporting.
* Added compile / build settings in build.sbt file. Now users can generate uber-jar using `sbt assembly` command
