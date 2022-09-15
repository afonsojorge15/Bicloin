# Guião de Demonstração


## 1. Preparação do sistema

Para testar o sistema e todos os seus componentes, é necessário preparar um ambiente com dados para proceder à verificação dos testes.

### 1.1. Lançar o *registry*

Para lançar o *ZooKeeper*, ir à pasta `zookeeper/bin` e correr o comando  
`./zkServer.sh start` (Linux) ou `zkServer.cmd` (Windows).

É possível também lançar a consola de interação com o *ZooKeeper*, novamente na pasta `zookeeper/bin` e correr `./zkCli.sh` (Linux) ou `zkCli.cmd` (Windows).

### 1.2. Compilar o projeto

Primeiramente, é necessário compilar e instalar todos os módulos e as suas dependências --  *rec*, *hub*, *app*, etc.
Para isso, basta ir à pasta *root* do projeto e correr o seguinte comando:

```sh
$ mvn clean install -DskipTests
```

### 1.3. Lançar e testar os *recs*

Para proceder aos testes, é preciso em primeiro lugar lançar os servidores *rec* .
Para isso basta ir à pasta *rec* e executar:

```sh
$ mvn exec:java -Dexec.args="localhost 2181 localhost 8091 1"
$ mvn exec:java -Dexec.args="localhost 2181 localhost 8092 2"
$ mvn exec:java -Dexec.args="localhost 2181 localhost 8093 3"
```

Este comando vai criar 3 servidores *rec* nos respetivos endereços.

Para confirmar o funcionamento dos servidores com um *ping*, fazer:

```sh
$ cd rec-tester
$ mvn compile exec:java -Dexec.args="localhost 2181"
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.


### 1.4. Lançar e testar o *hub*

Para proceder aos testes, é preciso em primeiro lugar lançar o servidor *hub*. Para isso basta ir à pasta *hub* e executar:

```sh
$ mvn exec:java -Dexec.args="localhost 2181 localhost 8081 1 ../demo/users.csv ../demo/stations.csv initRec"
```

Este comando vai colocar o *hub* no endereço *localhost* e na porta *8081*.

Para confirmar o funcionamento do servidor com um *ping*, fazer:

```sh
$ cd hub-tester
$ mvn compile exec:java
```

Para executar toda a bateria de testes de integração, fazer:

```sh
$ mvn verify
```

Todos os testes devem ser executados sem erros.

Depois de ter concluido os testes, pressionar a tecla *enter* para encerrar o server. 



### 1.5. *App*

Iniciar a aplicação, na pasta *app*, com a utilizadora alice:

```sh
$ mvn exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7380 -9.3000"
```

**Nota:** Para poder correr o script *app* diretamente é necessário fazer `mvn install` e adicionar ao *PATH* ou utilizar diretamente os executáveis gerados na pasta `target/appassembler/bin/`.

Abrir outra consola, e iniciar a aplicação com o utilizador bruno.

Depois de lançar todos os componentes, tal como descrito acima, já temos o que é necessário para usar o sistema através dos comandos.

## 2. Teste dos comandos

Nesta secção vamos correr os comandos necessários para testar todas as operações do sistema.
Cada subsecção é respetiva a cada operação presente no *hub*.


### 2.1. *balance*

Para verificar o *balance* da alice, fazemos:

    > balance

Que deve retornar:

    alice 0 BIC

Assumindo que ela ainda não fez nenhum carregamento nem nenhuma compra.

### 2.2 *top-up*

Para fazer um carregamento de 15 euros, executar:

    > top-up 15

Que deve retornar:
    
    alice 150 BIC

Dado que os valores dos carregamentos estão entre 1 e 20, caso se execute:

    > top-up 21

O servidor deve retornar:

    ERROR: Deposit amount must be between 1 and 20 euros


### 2.3 *info_station*

Para sabermos a informação sobre a estação *istt*, por exemplo, fazer:

    > info istt

Que deve retornar:

    IST Taguspark, lat 38.7372, -9.3023 long, 20 docks, 4 BIC prize, 12 bikes, 0 levantamentos, 0 devoluções, https://www.google.com/maps/place/38.7372,-9.3023

Caso se insira uma estação que não exista, o servidor retorna:

    ERROR: No Station found with abrev blaa

### 2.3 *locate_station*

Para sabermos quais as 3 estações que estão mais perto, fazer:

    > scan 3

Que deve retornar:

    istt, lat 38.7372, -9.3023 long, 20 docks, 4 BIC prize, 12 bikes, at 218 meters.
    stao, lat 38.6867, -9.3124 long, 30 docks, 3 BIC prize, 20 bikes, at 5804 meters.
    jero, lat 38.6972, -9.2064 long, 30 docks, 3 BIC prize, 20 bikes, at 9301 meters.


### 2.4 *bike_up*

Para levantar uma bicicleta podemos executar:

    > bike-up istt

Como o utilizador está longe da estação, o servidor retorna erro: 

    ERROR: Too far from station (min 200m)

Para levantar uma bicicleta com sucesso podemos executar:

    > move 38.7376 -9.3031
    > bike-up istt

Que deve retornar:

    alice at https://www.google.com/maps/place/38.7376,-9.3031
    OK

Se logo a seguir a executar estes comandos fizermos:

    > bike-up istt

O servidor retorna:
    
    ERROR: Cannot lift 2 bikes at the same time



### 2.5 *bike_down*

Para devolver uma bicicleta podemos executar:
    
    > move 38.7376 -9.3031
    > bike-down istt

Se logo a seguir a executar estes comandos fizermos:

    > bike-down istt

O servidor retorna:

    ERROR: No bike lifted

    

### 2.6 *ping*

Para enviarmos um sinal de vida ao servidor, executar:

    > ping

Ao qual o servidor deve retornar:

    OK: localhost:8081

Em caso de erro deve retornar:

    Warn: Server not responding!

### 2.7 *sys_status*

Para saber quais os servidores disponíveis, fazer:

    > sys_status

Caso só esteja ligado o servidor hub inicial, deve ser retornado:

    OK: localhost:8081
    OK: localhost:8091
    OK: localhost:8092
    OK: localhost:8093


----

## 3. Considerações Finais

Estes testes não cobrem tudo, pelo que devem ter sempre em conta os testes de integração e o código.

É possivel correr diretamente os comandos contidos num file do tipo *.txt*:

```sh
$ mvn exec:java -Dexec.args="localhost 2181 alice +35191102030 38.7380 -9.3000" < ../demo/app_commands.txt
```

Para além dos comandos referidos, existem também os comandos <code>zzz</code>, <code>tag</code> e <code>move</code> que auxiliam na simulação do sistema,
o comando <code>help</code> e o comando <code>quit</code>.

Após a execução dos comandos pretendidos, é possível encerrar os servidores de forma segura premindo a tecla *enter*.
