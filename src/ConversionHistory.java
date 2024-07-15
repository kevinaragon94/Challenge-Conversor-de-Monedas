import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ConversionHistory {
    private List<ConversionRecord> history;

    public ConversionHistory() {
        history = new ArrayList<>();
    }

    public void addRecord(double amount, String fromCurrency, String toCurrency, double rate, double convertedAmount) {
        history.add(new ConversionRecord(amount, fromCurrency, toCurrency, rate, convertedAmount, LocalDateTime.now()));
    }

    public void showHistory() {
        if (history.isEmpty()) {
            System.out.println("No hay historial de conversiones.");
        } else {
            for (ConversionRecord record : history) {
                System.out.println(record);
            }
        }
    }
}
