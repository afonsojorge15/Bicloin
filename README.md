# T03-Bicloin

Distributed Systems 2020-2021, 2nd semester project


## Authors

**Group T03**

Afonso Jorge [93680](afonso.jorge@tecnico.ulisboa.pt)
![photo](https://imgur.com/OYZWCHd.png)

Guilherme Saraiva [93717](guilherme.a.saraiva@tecnico.ulisboa.pt)
![photo](https://imgur.com/lOU6b7C.png)

Sara Ferreira [93756](sara.c.ferreira@tecnico.ulisboa.pt)
![photo](https://imgur.com/2oJnh8I.png)

### Module leaders

| Task set | To-Do                               | Leader              |
| ---------|-------------------------------------| --------------------|
| core     | protocol buffers; project structure | _(whole team)_      |
| T1       | rec: read, write; hub-tester        | _Sara Ferreira_     |
| T2       | hub; rec-tester                     | _Guilherme Saraiva_ |
| T3       | info_station; app                   | _Afonso Jorge_      |


### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.

This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.


## Getting Started

The overall system is composed of multiple modules.

See the [project statement](https://github.com/tecnico-distsys/Bicloin/blob/main/part1.md) for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```
mvn clean install -DskipTests
```

The integration tests are skipped because they require theservers to be running.


## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
