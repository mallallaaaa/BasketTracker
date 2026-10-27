package baskettracker;

import java.time.LocalDate;

// Represents a single activity with its date, type and duration.
public record Attivita(LocalDate data, String tipo, int minuti) {}
