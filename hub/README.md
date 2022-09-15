# Server


## Authors

Group T03


### Lead developer 

Guilherme Saraiva 93717 [guisaraiva2000](https://github.com/guisaraiva2000)

### Contributors

Afonso Jorge 93680 [Afonso-Jorge](https://github.com/Afonso-Jorge)

Sara Ferreira 93756 [SaraCFerreira](https://github.com/SaraCFerreira)


## About

This is a gRPC server defined by the protobuf specification.

The server runs in a stand-alone process.


## Instructions for using Maven

To compile and run the first server:

```
mvn compile exec:java -Dexec.args="localhost 2181 localhost 8081 1 ../demo/users.csv ../demo/stations.csv initRec"
```

When running, the server await connections from clients.


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

