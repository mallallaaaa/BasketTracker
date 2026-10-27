package baskettracker;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BasketTracker {
    // Defines the date format used for user input and output.
    // STRICT ensures that invalid dates are rejected.
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    // Scanner used to read user input from the terminal.
    private static final Scanner INPUT = new Scanner(System.in);

    // List containing all the recorded activities.
    private static final List<Attivita> attivita = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Load previously saved activities from the file.
        attivita.addAll(ArchivioAttivita.carica());

        while (true) {
            System.out.println("\n=== BASKET TRACKER ===");
            System.out.println("1. Aggiungi allenamento (1h 30m)");
            System.out.println("2. Aggiungi partita");
            System.out.println("3. Riepilogo di un mese");
            System.out.println("4. Riepilogo di tutti i mesi");
            System.out.println("0. Esci");
            System.out.print("Scelta: ");

            switch (INPUT.nextLine().trim()) {
                case "1" -> aggiungi("A");
                case "2" -> aggiungi("P");
                case "3" -> riepilogoMese();
                case "4" -> Riepilogo.stampaTutti(attivita);
                case "0" -> { return; }
                default -> System.out.println("Scelta non valida.");
            }
        }
    }

    private static void aggiungi(String tipo) throws IOException {
        LocalDate data = leggiData();
        if (data == null) {
            return;
        }

        Integer minuti;
        if(tipo.equals("A")){
            minuti = 90;
        } else {
            minuti = leggiDurataPartita();
            if (minuti == null) {
                return;
            }
        }

        // Warn if a match is not scheduled on a weekend.
        if (tipo.equals("P") && data.getDayOfWeek() != DayOfWeek.SATURDAY
                && data.getDayOfWeek() != DayOfWeek.SUNDAY) {
            System.out.println("Nota: questa partita non è nel weekend (va bene comunque).");
        }

        // Create a new activity and save it both to the file and the list.
        Attivita nuova = new Attivita(data, tipo, minuti);
        ArchivioAttivita.salva(nuova);
        attivita.add(nuova);

        String descrizione;
        if(tipo.equals("A")){
            descrizione = "allenamento";
        } else {
            descrizione = "partita";
        }

        System.out.printf("Salvato: %s del %s, durata %s.%n", descrizione, data.format(DATA), Riepilogo.ore(minuti));
    }

    // Reads a date from the terminal and validates its format.
    private static LocalDate leggiData() {

        // Keep asking until the user enters a valid date.
        while (true) {
            System.out.print("Data (gg/mm/aaaa, 0 = torna al menu): ");
            String risposta = INPUT.nextLine().trim();
            if (risposta.equals("0")) {
                return null;
            }

            try {
                // Convert the entered text into a LocalDate using DATA.
                return LocalDate.parse(risposta, DATA);

            } catch (DateTimeParseException e) {
                // Handle invalid dates without terminating the program.
                System.out.println("Data non valida. Esempio: 22/10/2026");
            }
        }
    }


    // Reads the duration of a match and converts it into minutes.
    private static Integer leggiDurataPartita() {

        // Keep asking until the user enters a valid duration.
        while (true) {
            System.out.print("Durata partita (ore:minuti, es. 2:15, 0 = torna al menu): ");
            String durata = INPUT.nextLine().trim();
            if (durata.equals("0")) {
                return null;
            }

            // Check if the input matches the hours:minutes format.
            // \\d+ means one or more digits (hours).
            // [0-5]\\d means minutes between 00 and 59.
            if (durata.matches("\\d+:[0-5]\\d")) {

                // Split the input into hours and minutes.
                String[] parti = durata.split(":");

                try {
                    // Convert hours and minutes into integers.
                    // Multiply hours by 60 and add the remaining minutes.
                    int minuti = Math.addExact(
                            Math.multiplyExact(Integer.parseInt(parti[0]), 60),
                            Integer.parseInt(parti[1])
                    );

                    // Return the duration only if it is positive.
                    if (minuti > 0) {
                        return minuti;
                    }

                } catch (ArithmeticException ignored) {
                    // Handle durations that are too large.
                }
            }

            // Display an error message and ask for the duration again.
            System.out.println("Durata non valida. Esempi: 1:30, 2:00, 2:45");
        }
    }

    // Displays all activities and their totals for a specific month.
    private static void riepilogoMese() {
        YearMonth mese;

        // Ask the user to enter a valid year and month.
        while (true) {
            System.out.print("Mese (aaaa-mm, es. 2026-10, 0 = torna al menu): ");
            String risposta = INPUT.nextLine().trim();
            if (risposta.equals("0")) {
                return;
            }

            try {
                // Convert the input into a YearMonth object.
                mese = YearMonth.parse(risposta);

                // Exit the loop when the input is valid.
                break;

            } catch (DateTimeParseException e) {
                System.out.println("Formato non valido.");
            }
        }

        Riepilogo.stampaMese(attivita, mese);
    }
}
