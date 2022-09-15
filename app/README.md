# Application


## Authors

Group T03


### Lead developer

Afonso Jorge 93680 [Afonso-Jorge](https://github.com/Afonso-Jorge)

### Contributors

Guilherme Saraiva 93717 [guisaraiva2000](https://github.com/guisaraiva2000)

Sara Ferreira 93756 [SaraCFerreira](https://github.com/SaraCFerreira)


## About

This is a CLI (Command-Line Interface) application.


## Instructions for using Maven

To compile and run using _exec_ plugin:

```
mvn exec:java -Dexec.args="localhost 2181 arg2 arg3 arg4 arg5"
```

To generate launch scripts for Windows and Linux
(the POM is configured to attach appassembler:assemble to the _install_ phase):

```
mvn install
```

To run using appassembler plugin on Linux:

```
./target/appassembler/bin/app localhost 2181 arg2 arg3 arg4 arg5
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\app localhost 2181 arg2 arg3 arg4 arg5
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

