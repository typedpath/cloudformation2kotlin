# Aurora Backend Api
This example provides generic typesafe CRUD storage backed by a REST api pointing at a relational schema located on an Aurora serverless cluster.

__generic__ meaning that the example will work with any object model defined with the same conventions as the model used in this example (defined in com.typedpath.testdomain.Sample.kt).  i.e all data is nested.

__typesafe__ meaining that objects cant be saved with the wrong types or field names.

The service is wrapped on the client side with __com.typedpath.awscloudformation.test.serverless.typesafebackendapi.Saver__ which is used like this:

```kotlin
        //save an object
        val id = saver.save(team)
      
        // retrieve an object
        val teamOut = saver.retrieve(Team::class, id)

        //update part of the object 
        val personToAmend = team.members.get(0)
        personToAmend.firstName = "Moddy"
        personToAmend.lastName = "Fied"
        saver.save(entryToAmend.value, setOf(personToAmend))

        //delete
        saver.delete(team)
```

It doesnt use any libraries other than the kotlin standard libraries. 

Its very basic - only String properties are supported and data is assumed to be contained only e.g. a Team instance contains everything below it. Database ids are managed outside of the domain objects. 

All the serialization code is contained in file com.typedpath.testdomain.Serialize.kt.  This serializer is b friendly - it manages db ids and db actions.

All the db code is contained in __com.typedpath.aurora.AuroraStorer__.  In addition to low level CRUD __AuroraStorer.kt__ also provides 2 operations that can't be specified in cloudformation:

```kotlin
//enable rds data REST interface
   enableHttpEndpoint(region, credentialsProvider, dbClusterIdentifier.outputValue)

    val storer = AuroraStorer(dbClusterArn, secretArn, schemaName)
    // create database schema for Team and linked classes
    storer.createSchema(Team::class, createDb)

```



