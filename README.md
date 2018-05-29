# MulinoChallange

Progetto sviluppato per la competizione "Gioco del mulino A.A. 2017/2018" del corso di Fontamenti di intelligenza artificiale.

|         TEAM        |
|---------------------|
|   Giovanni Baratta  |
| Roberto Della Penna |
|   Lorenzo Antonini  |

# Parametri per lanciare il client:

Per il giocatore bianco -> `java -jar client.jar White MinMax`

Per il giocatore nero -> `java -jar client.jar Black MinMax`

# Descrizione

Il client utilizza un algoritmo di Iterative Deepening Seach con tagli Alpha-Beta. La funzione di valutazione degli stati è basata sul paper [Nine Men’s Morris: Evaluation Functions](http://dasconference.ro/papers/2008/B7.pdf) di PETCU e HOLBAN, con qualche piccola modifica. Per massimizzare le performance è stato utilizzata una rappresentazione dello stato basata unicamente su due interi, uno per ogni player.

Link github del progetto : https://github.com/giovannibaratta/MulinoChallange
