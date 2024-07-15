import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    // Crear un logger para la clase Main
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    // Mapa para almacenar los nombres completos de las monedas
    private static final Map<String, String> currencyNames = new HashMap<>();

    public static void main(String[] args) {
        String apiKey = "TU_API_KEY";
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/USD";

        // Crear un cliente HTTP para hacer solicitudes
        HttpClient client = HttpClient.newHttpClient();
        // Crear una solicitud HTTP GET para obtener los datos de la API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // Enviar la solicitud de forma asíncrona y procesar la respuesta
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)  // Extraer el cuerpo de la respuesta
                .thenAccept(Main::parse)        // Pasar el cuerpo a la función parse
                .join();                       // Esperar a que la tarea asíncrona se complete
    }

    // Función para procesar la respuesta de la API
    public static void parse(String responseBody) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

        // Cargar los nombres de monedas desde el archivo JSON
        try (InputStream inputStream = Main.class.getResourceAsStream("/currencyNames.json")) {
            if (inputStream == null) {
                throw new RuntimeException("No se pudo encontrar el archivo currencyNames.json.");
            }
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonObject currencyNamesObject = JsonParser.parseReader(reader).getAsJsonObject();
                // Rellenar el mapa con el código de moneda y su nombre completo
                for (Map.Entry<String, com.google.gson.JsonElement> entry : currencyNamesObject.entrySet()) {
                    currencyNames.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al leer el archivo currencyNames.json: " + e.getMessage(), e);
            return;  // Salir del método si no se puede leer el archivo
        }

        ConversionHistory history = new ConversionHistory();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Mostrar el menú de opciones al usuario
            System.out.println("Seleccione una opción:");
            System.out.println("1. Convertir monedas");
            System.out.println("2. Ver historial de conversiones");
            System.out.println("3. Salir");

            // Obtener la opción del usuario con validación de entero
            int option = getIntInput(scanner);
            if (option == 3) {
                break;  // Salir del bucle si la opción es 3
            }

            switch (option) {
                case 1:
                    System.out.println("Ingrese la cantidad:");
                    double amount = getDoubleInput(scanner);  // Obtener la cantidad con validación de decimal

                    System.out.println("Seleccione la moneda de origen:");
                    printAvailableCurrencies(conversionRates);  // Mostrar las monedas disponibles

                    String fromCurrency = getCurrencyInput(scanner, conversionRates);  // Obtener la moneda de origen con validación

                    System.out.println("Seleccione la moneda de destino:");
                    printAvailableCurrencies(conversionRates);  // Mostrar las monedas disponibles

                    String toCurrency = getCurrencyInput(scanner, conversionRates);  // Obtener la moneda de destino con validación

                    if (conversionRates.has(fromCurrency) && conversionRates.has(toCurrency)) {
                        // Obtener las tasas de cambio para las monedas seleccionadas
                        double fromRate = conversionRates.get(fromCurrency).getAsDouble();
                        double toRate = conversionRates.get(toCurrency).getAsDouble();
                        double rate = toRate / fromRate;
                        double convertedAmount = convert(amount, rate);  // Convertir la cantidad

                        // Obtener el nombre completo de las monedas para mostrar
                        String fromCurrencyName = currencyNames.getOrDefault(fromCurrency, "Desconocido");
                        String toCurrencyName = currencyNames.getOrDefault(toCurrency, "Desconocido");
                        // Mostrar el resultado de la conversión
                        System.out.printf("$ %.2f %s (%s) a %s (%s) $ %.2f\n", amount, fromCurrency, fromCurrencyName, toCurrency, toCurrencyName, convertedAmount);
                        history.addRecord(amount, fromCurrency, toCurrency, rate, convertedAmount);  // Guardar el historial de conversión
                    } else {
                        System.out.println("Moneda no válida.");  // Mensaje de error si la moneda no es válida
                    }
                    break;

                case 2:
                    history.showHistory();  // Mostrar el historial de conversiones
                    break;

                default:
                    System.out.println("Opción inválida");  // Mensaje de error si la opción no es válida
                    break;
            }
        }
    }

    // Función para imprimir las monedas disponibles en forma de lista
    public static void printAvailableCurrencies(JsonObject conversionRates) {
        System.out.println("Monedas disponibles:");
        for (String currency : conversionRates.keySet()) {
            String name = currencyNames.getOrDefault(currency, "Desconocido");
            System.out.printf("%s: %s\n", currency, name);
        }
        System.out.println();
    }

    // Función para convertir una cantidad usando una tasa de cambio
    public static double convert(double amount, double rate) {
        return amount * rate;
    }

    // Función para obtener una entrada entera del usuario con validación
    public static int getIntInput(Scanner scanner) {
        while (true) {
            if (scanner.hasNextInt()) {
                return scanner.nextInt();
            } else {
                System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
                scanner.next();  // Limpiar el buffer de entrada
            }
        }
    }

    // Función para obtener una entrada decimal del usuario con validación
    public static double getDoubleInput(Scanner scanner) {
        while (true) {
            if (scanner.hasNextDouble()) {
                return scanner.nextDouble();
            } else {
                System.out.println("Entrada inválida. Por favor, ingrese un número decimal.");
                scanner.next();  // Limpiar el buffer de entrada
            }
        }
    }

    // Función para obtener una moneda válida del usuario con validación
    public static String getCurrencyInput(Scanner scanner, JsonObject conversionRates) {
        while (true) {
            String input = scanner.next().toUpperCase();  // Leer la entrada y convertir a mayúsculas
            if (currencyNames.containsKey(input) && conversionRates.has(input)) {
                return input;  // Devolver la moneda si es válida
            } else {
                System.out.println("Moneda no válida. Por favor, ingrese una moneda válida.");
                printAvailableCurrencies(conversionRates);  // Mostrar opciones válidas
            }
        }
    }
}
