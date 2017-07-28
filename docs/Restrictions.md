# Technical Restrictions of JCloudScale Lambda

* Currently there is no way to pass method arguments by-reference.

* Inner classes are not completely supported. The use of inner classes is generally not a problem, but an inner class should not have any method with a CloudMethod annotation, because jCloudScale Lambda has no access to non-public classes.

* The jCloudScale Lambda does not support any file system operations.

* There is no possibility to create a synchronized variable or method. A variable cannot be blocked until the execution of the method is complete. Therefore, a serverless function can request the current value of a variable and calculate a new one from this value. After the new value is calculated, the value is sent over the message queue to the client, where the updated value is saved. During the calculation of the new value, another serverless method requests the variable. The second method receives the old value and maybe works with an wrong variable value. There is no possibility to not send the current value of the variable to the second method, until the first method has modified the value. A developer must pay attention to such situations.

* Java automatically creates an empty no-argument constructor if the developer does not define any constructor. JCloudScale Lambda requires the no-argument constructor to initialize an object. It is recommended that the no-argument constructor is empty, because otherwise the content of this constructor is always executed from the framework.

* Generics such as an ArrayList are supported from jCloudScale Lambda with by-value passing. By-reference passing with generics is so far not possible, because the GSON parser has some problems during the deserialization process with types defined at runtime, which includes generics.

* The access to by-reference passed variables is restricted, because the AspectJ framework requires a this context to set or get a variable. Currently it is impossible to get the value of a static variable with a ByReference annotation without being in the this context of the variable.

* It is never permissible to use the default package from Java in combination with jCloudScale Lambda.

* Functionalities such as lambda expression or stream collection types, which were introduced with Java 8, work, but the development focus was not on these features.

* If the developer has multiple applications that use the jCloudScale Lambda framework, each should have an separated SQS queue. If an application is already running and a second application with the same message queue is started, the started application purges the message queue. Possibly the already running application loses some messages and will never terminate.

* If the queue is initialized, the queue is purged and all messages are deleted. However, Amazon allows only to purge a queue once a minute. If Amazon blocks the purging request, the queue is normally not very efficient, and in extreme cases the application fails.

* At the moment there is a concurrent Lambda function execution limit of 1000 parallel executions per region.

* The API Gateway has a throttle rate of 10000s request per second.

* A HTTP request over the Gateway API is stopped after 30 seconds . The user gets a 500 error message. The started Lambda function is not stopped, but the return value cannot be passed back to the invoker.

* The Lambda function deployment package size is limited to 50 MB. From these 50 MB, 15 MB are used by the jCloudScale Lambda framework with all required dependencies.

* The ephemeral disk capacity ("/tmp" space) is limited to 512 MB per invocation.

* In one Amazon region, the total size of all Lambda functions is limited to 75 GB.

* The memory allocation capacity of a serverless function can be chosen between a minimum of 128 MB and a maximum of 1536 MB in 64 MB increments.

* The SQS has a limited message size of 256 KB. Currently it is impossible to use by-reference variables, which are larger.