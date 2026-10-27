package baskettracker;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Riepilogo {
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu");

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

    // Displays all activities and their totals for a specific month.
    public static void stampaMese(List<Attivita> attivita, YearMonth mese) {
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
    public static void stampaTutti(List<Attivita> attivita) {

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
    public static String ore(int minuti) {

        // Integer division gives the number of complete hours.
        // The remainder gives the remaining minutes.
        return String.format("%dh %02dm", minuti / 60, minuti % 60);
    }

}
