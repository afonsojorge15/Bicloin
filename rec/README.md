# Server


## Authors

Group T03

### Lead developer

Sara Ferreira 93756 [SaraCFerreira](https://github.com/SaraCFerreira)

### Contributors

Guilherme Saraiva 93717 [guisaraiva2000](https://github.com/guisaraiva2000)

Afonso Jorge 93680 [Afonso-Jorge](https://github.com/Afonso-Jorge)


## About

This is a gRPC server defined by the protobuf specification.

The server runs in a stand-alone process.

It is important to have ZooKeeper running in the background for the server to work properly.


## Instructions for using Maven

To compile and run:

```
mvn exec:java -Dexec.args="localhost 2181 localhost 8091 1"
```

For multiple servers just add 1 to the server port and instance, for example:

```
mvn exec:java -Dexec.args="localhost 2181 localhost 8092 2"
```

And so on...

When running, the server awaits connections from clients.


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

