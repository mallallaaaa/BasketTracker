# Basket Tracker

Applicazione da terminale per registrare allenamenti e partite di basket e visualizzare i riepiloghi mensili.

## Requisiti

JDK 17 o successivo, con `java` e `javac` disponibili nel terminale.

## Compilazione e avvio

Dalla cartella principale del progetto:

```sh
javac -encoding UTF-8 -d out src/baskettracker/*.java
java -cp out baskettracker.BasketTracker
```

## Organizzazione del codice

- `src/baskettracker/BasketTracker.java`: menu, lettura e validazione degli input.
- `src/baskettracker/Attivita.java`: dati di un allenamento o di una partita.
- `src/baskettracker/ArchivioAttivita.java`: caricamento e salvataggio del CSV.
- `src/baskettracker/Riepilogo.java`: calcolo dei totali e stampa dei riepiloghi.

## Utilizzo e dati

Gli allenamenti durano 90 minuti; per le partite viene richiesta la durata.
Durante l'inserimento di data, durata o mese, digita `0` per tornare al menu senza salvare l'attività in corso. Nel menu principale, `0` chiude il programma.

I dati vengono salvati nel file `.basket-tracker.csv` nella cartella utente. La riorganizzazione del codice mantiene lo stesso archivio e il formato esistente.
