# Relatório do projeto *Bicloin*

Sistemas Distribuídos 2020-2021, segundo semestre

## Autores

**Grupo T03**

![Afonso](https://imgur.com/XGfNLW9.png) ![Guilherme](https://imgur.com/MH8Z9Uz.png) ![Sara](https://imgur.com/N0nvglF.png)


| Número | Nome                    | Utilizador                                     | Correio eletrónico                                  |
| -------|-------------------------|------------------------------------------------| ----------------------------------------------------|
| 93680  | Afonso Jorge            | <https://git.rnl.tecnico.ulisboa.pt/ist193680> | <mailto:afonso.jorge@tecnico.ulisboa.pt>            |
| 93717  | Guilherme Saraiva       | <https://git.rnl.tecnico.ulisboa.pt/ist193717> | <mailto:guilherme.a.saraiva@tecnico.ulisboa.pt>     |
| 93756  | Sara Ferreira           | <https://git.rnl.tecnico.ulisboa.pt/ist193756> | <mailto:sara.c.ferreira@tecnico.ulisboa.pt>         |


## Melhorias da primeira parte

Apesar da primeira entrega estar funcional, esta ainda apresentava pequenas falhas.
Estas falhas davam-se ao nível do tratamento de exceções e da sincronização de variáveis partilhadas.

A correção destes erros pode ser encontrada no seguinte commit:

- [descrição da alteração](https://git.rnl.tecnico.ulisboa.pt/SD-20-21-2/T03-Bicloin/commit/52424cf32eb0dc6be3d79ed3d46f7006f05270fe)

Antes desta correção ter sido efetuada, os pedidos ao _rec_ não estavam sincronizados e podiam gerar valores errados ao cliente.
Para além disto, quando o hub falhava, a _app_ terminava abruptamente sem apresentar nenhuma mensagem de erro ao user.

## Modelo de faltas

A nossa implementação tolera todo o tipo de faltas. Quando um servidor falha, seja por fecho do terminal onde estava a correr
ou por ser colocado o processo em pausa, a _app_ continua a poder enviar e receber pedidos corretamente.
Isto acontece devido ao facto de que o servidor cliente (_hub_) envia o pedido a todos os servidores registados no _ZooKeeper_ e, caso algum 
falhe, o _hub_ recolhe respostas de outros servidores, evitando assim a perda de registos.
Este processo será explicado com rigor mais adiante.

## Solução

![UML](https://imgur.com/I7j4Bkt.png)

Pela análise do diagrama, podemos observar que a tolerância a faltas é garantida pelo envio de _requests_ por parte do _RecQuorumFrontend_
para todos os servidores, aguardando pelas respostas de _Quórum_ servidores. Assim, caso se dê alguma falta, nenhum registo é perdido.
Para garantir que os registos que chegam ao hub estão atualizados, é-lhes associado uma _tag_ em que o registo mais recente é o que tem maior tag.

## Protocolo de replicação

O protocolo de replicação implementado corresponde ao Protocolo Registo Coerente. Este permite fazer a troca de mensagens (registos) de forma coerente e disponível em múltiplos 
servidores (réplicas).

As réplicas são todas idênticas e executam em paralelo o mesmo serviço como máquinas de estado determinísticas.
O cliente envia mensagens às réplicas e, cada réplica, executa o pedido e responde de volta. 
De seguida, o cliente aguarda por um conjunto de respostas e retorna uma delas.

O conjunto de respostas advém da implementação de um sistema de Quóruns. Assim, em vez do cliente esperar pelas respostas de 
todos os servidores, apenas tem de esperar por respostas de um Quórum. Este Quórum é previamente conhecido pelo cliente e corresponde
ao número total de servidores a dividir por dois mais um. Assim, cada Quórum de escrita tem pelo menos uma réplica em
comum com cada Quórum de leitura ou de escrita.

As transições de mensagens executam se de forma assíncrona sendo a comunicação fiável, ou seja, mensagens enviadas são recebidas desde 
que remetente e destinatário não falhem.  

Para garantir a coerência, cada réplica vai guardar o valor do registo e uma _tag_ associada ao registo. Esta _tag_ é constituída por 
um _seq_ que corresponde ao número de sequência da escrita que deu origem à versão, e por um _cid_ que representa o número 
identificador do cliente. Assim, ao executar uma escrita, o servidor apenas guarda o registo que tiver maior _tag_ e, desta forma, 
garantir que os registos estarão sempre atualizados.

Nesta troca de mensagens, aquando dum _read_, a reṕlica envia ao servidor cliente o valor do registo e a sua _tag_ e aquando dum write,
o cliente envia o novo valor do registo, bem como a _tag_ atualizada devido ao incremento do _seq_, e a réplica devolve _ack_.

Em suma, o protocolo garante que quando um cliente lê um registo e não está a decorrer nenhuma escrita concorrente no registo,
o valor devolvido vai corresponder ao valor mais recente.

## Medições de desempenho

| Comando | Modelo Base | Primeira Fase de Otimização | Segunda Fase de Otimização                                  |
| ------- |-------------|-----------------------------| ----------------------------|
| read()  |  0.033s     |  0.022s                     |   0.015s                    |
| write() |  0.053s     |  0.045s                     |   0.037s                    |

Estes resultados foram obtidos com 3 servidores ativos (Quórum = 2) e com _timers_ colocados no início e no fim
das funções _read()_ e write() da classe _RecQuorumFrontend.java_.

Foram executados 5 _reads_ e 5 _writes_ em que o resultado final corresponde à média dos tempos de cada comando.

Para além do Modelo Base, foram realizadas mais duas otimizações que serão explicadas nas _Opções de implementação_.

## Opções de implementação

A nossa implementação começou com a criação da classe _RecQuorumFrontend_. Para além do Quórum, foi nesta classe que, ao contrário da entrega passada em que se usavam
_blocking stubs_, criámos uma lista de _non-bocking stubs_ para cada servidor registado no zookeeper. Estes novos stubs permitem 
a comunicação assíncrona das mensagens. Para que o cliente ficasse à espera das respostas dos servidores aquando dos seus pedidos,
utilizámos a ferramenta _CountDownLatch_ que só desbloqueava as _threads_ em espera, assim que todas as respostas chegassem.
De seguida, para guardar as respostas dos servidores após cada pedido, criámos a classe _ResponseCollector_ que nos permitiu
escolher a resposta a retornar ao cliente. A escolha da melhor resposta fez-se através da obtenção do maior _seq_ contido nos registos
guardados na lista.

No nosso modelo base funcional, esperávamos por respostas de todos os servidores e guardávamo-las no _ResponseCollector_ onde selecionávamos
as _Quórum_ primeiras respostas e escolhíamos a melhor para devolver ao cliente.

Ao perceber que esta não era a solução mais prática, avançámos com o desenvolvimento da primeira otimização que, consistiu em recolher as
respostas e, assim que se atingisse o valor do quórum, o _Collector_ parava de guardar as respostas e passava logo para a escolha da
melhor resposta.

Apesar de os tempos terem sido melhorados, ainda não eram ideais. Assim, ao invés de esperar pelas respostas de todos os servidores,
o _RecQuorumFrontend_ apenas vai esperar por _Quórum_ respostas. Esta nova _feature_ consistiu assim, na nossa segunda e última otimização que 
melhorou os tempos de leitura e escrita em relação à otimização anterior.


## Notas finais

Foi ainda testada uma implementação que consistiu em atribuir pesos variados aos _reads()_ e aos _writes()_ que permitia otimizar uma operação
à custa da outra, dado que em sistemas onde são executados mais _reads_ que _writes_ o peso das escritas seria muito maior. Como não vimos
nenhuma melhoria significativa e ainda apresentava certos erros, decidimos não submeter esta versão.