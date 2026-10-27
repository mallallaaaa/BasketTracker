
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class BasketTracker {
    // Path to the local file where all activites are stored
    private static final Path ARCHIVIO =
            Path.of(System.getProperty("user.home"), ".basket-tracker.csv");
    
    // Defines the date format used for user input and output.
    // STRICT ensures that invalid dates are rejected.
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    // Scanner used to read user input from the terminal.
    private static final Scanner INPUT = new Scanner(System.in);

    // List containing all the recorded activities.
    private static final List<Attivita> attivita = new ArrayList<>();

    // Represents a single activity with its date, type and duration.
    record Attivita(LocalDate data, String tipo, int minuti) {}

    // Stores the total number of activities and their durations.
    static class Totali {
        int allenamenti, minutiAllenamenti, partite, minutiPartite;

        void aggiungi(Attivita a) {
            if (a.tipo().equals("A")) {
                allenamenti++;
                minutiAllenamenti += a.minuti();
            } else {
                partite++;
                minutiPartite += a.minuti();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Load previously saved activities from the file.
        carica();

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
                case "4" -> riepilogoTutti();
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
        salva(nuova);
        attivita.add(nuova);

        String descrizione;
        if(tipo.equals("A")){
            descrizione = "allenamento";
        } else {
            descrizione = "partita";
        }

        System.out.printf("Salvato: %s del %s, durata %s.%n", descrizione, data.format(DATA), ore(minuti));
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

        // Create an object to store the monthly totals.
        Totali totali = new Totali();

        // Create a list to store only activities from the selected month.
        List<Attivita> delMese = new ArrayList<>();

        // Iterate through all recorded activities.
        for (Attivita a : attivita) {

            // Check whether the activity belongs to the selected month.
            if (YearMonth.from(a.data()).equals(mese)) {
                delMese.add(a);
            }
        }

        // Sort the monthly activities by date in ascending order.
        delMese.sort(Comparator.comparing(Attivita::data));

        // Add each activity to the monthly totals.
        for (Attivita a : delMese) {
            totali.aggiungi(a);
        }

        // Print the month and its overall statistics.
        System.out.println("\n--- " + mese + " ---");
        stampaTotali(totali);

        // Print the details of every activity in the selected month.
        for (Attivita a : delMese) {

            // Determine whether the activity is a training session or a match.
            String descrizione;

            if (a.tipo().equals("A")) {
                descrizione = "Allenamento";
            } else {
                descrizione = "Partita";
            }

            // Print the date, activity type and duration.
            // %-12s left-aligns the description in a field of at least 12 characters.
            System.out.printf("  %s  %-12s %s%n",
                    a.data().format(DATA),
                    descrizione,
                    ore(a.minuti()));
        }
    }

    // Displays the monthly totals for all recorded months.
    private static void riepilogoTutti() {

        // Store monthly totals in chronological order using a TreeMap.
        // Key: YearMonth, Value: Totali.
        Map<YearMonth, Totali> mesi = new TreeMap<>();

        // Iterate through all recorded activities.
        for (Attivita a : attivita) {

            // Extract the year and month of the current activity.
            YearMonth mese = YearMonth.from(a.data());

            // Get the totals associated with this month.
            Totali totali = mesi.get(mese);

            // If the month does not exist in the map, create a new Totali object.
            if (totali == null) {
                totali = new Totali();

                // Associate the new totals with the corresponding month.
                mesi.put(mese, totali);
            }

            // Update the totals with the current activity.
            totali.aggiungi(a);
        }

        // Check if there are no recorded activities.
        if (mesi.isEmpty()) {
            System.out.println("Non hai ancora registrato attività.");
            return;
        }

        // Iterate through the months in chronological order.
        for (Map.Entry<YearMonth, Totali> voce : mesi.entrySet()) {

            // Print the current month.
            System.out.println("\n--- " + voce.getKey() + " ---");

            // Print the statistics associated with the current month.
            stampaTotali(voce.getValue());
        }
    }

    // Prints the number of training sessions, matches and total hours.
    private static void stampaTotali(Totali t) {

        // Print the number of training sessions and their total duration.
        System.out.printf("Allenamenti: %d  (%s)%n",
                t.allenamenti, ore(t.minutiAllenamenti));

        // Print the number of matches and their total duration.
        System.out.printf("Partite:     %d  (%s)%n",
                t.partite, ore(t.minutiPartite));

        // Print the combined duration of training sessions and matches.
        System.out.printf("TOTALE:         %s%n",
                ore(t.minutiAllenamenti + t.minutiPartite));
    }

    // Converts a duration in minutes into a readable hours/minutes format.
    private static String ore(int minuti) {

        // Integer division gives the number of complete hours.
        // The remainder gives the remaining minutes.
        return String.format("%dh %02dm", minuti / 60, minuti % 60);
    }

    // Saves a new activity to the local CSV file.
    private static void salva(Attivita a) throws IOException {

        // Create a line containing the date, type and duration.
        // The semicolon separates the three fields.
        String riga = a.data() + ";" + a.tipo() + ";" + a.minuti() + "\n";

        // Append the activity to the file without deleting existing data.
        Files.writeString(
                ARCHIVIO,
                riga,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    // Loads previously saved activities from the local CSV file.
    private static void carica() throws IOException {

        // If the file does not exist, there are no activities to load.
        if (!Files.exists(ARCHIVIO)) {
            return;
        }

        // Read the file line by line.
        for (String riga : Files.readAllLines(ARCHIVIO, StandardCharsets.UTF_8)) {

            try {
                // Split each line into date, activity type and duration.
                String[] campi = riga.split(";");

                // Skip lines with an incorrect number of fields
                // or an invalid activity type.
                if (campi.length != 3 ||
                        (!campi[1].equals("A") && !campi[1].equals("P"))) {
                    continue;
                }

                // Convert the saved values into an Attivita object
                // and add it to the list of recorded activities.
                attivita.add(new Attivita(
                        LocalDate.parse(campi[0]),
                        campi[1],
                        Integer.parseInt(campi[2])
                ));

            } catch (RuntimeException e) {

                // Ignore invalid lines and continue loading the file.
                System.out.println("Ignorata una riga non valida nell'archivio.");
            }
        }
    }
}
