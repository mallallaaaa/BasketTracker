package baskettracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ArchivioAttivita {
    // Path to the local file where all activities are stored.
    private static final Path ARCHIVIO =
            Path.of(System.getProperty("user.home"), ".basket-tracker.csv");

    // Saves a new activity to the local CSV file.
    public static void salva(Attivita a) throws IOException {

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
    public static List<Attivita> carica() throws IOException {
        List<Attivita> attivita = new ArrayList<>();

        // If the file does not exist, there are no activities to load.
        if (!Files.exists(ARCHIVIO)) {
            return attivita;
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
        return attivita;
    }
}
