import java.time.LocalDateTime;

class ConversionRecord {
    private double amount;
    private String fromCurrency;
    private String toCurrency;
    private double rate;
    private double convertedAmount;
    private LocalDateTime timestamp;

    public ConversionRecord(double amount, String fromCurrency, String toCurrency, double rate, double convertedAmount, LocalDateTime timestamp) {
        this.amount = amount;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.convertedAmount = convertedAmount;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s: $ %f %s a %s a tasa %f = %f", timestamp, amount, fromCurrency, toCurrency, rate, convertedAmount);
    }
}
