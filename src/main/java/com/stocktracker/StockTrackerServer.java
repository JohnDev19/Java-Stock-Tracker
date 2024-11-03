import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class StockTrackerServer {
    private static final int PORT = 8080;
    private static final Map<String, Stock> stockDatabase = new HashMap<>();

    static {
        initializeStockDatabase();
    }

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/", new StockTrackerHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Server is running on http://localhost:" + PORT);
            System.out.println("Press Ctrl+C to stop the server.");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class StockTrackerHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Stock Tracker</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            margin: 0;
                            padding: 20px;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 800px;
                            margin: 0 auto;
                            background-color: #fff;
                            padding: 20px;
                            border-radius: 5px;
                            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        }
                        h1 {
                            text-align: center;
                            color: #2c3e50;
                        }
                        #stockForm {
                            display: flex;
                            gap: 10px;
                            margin-bottom: 20px;
                        }
                        #symbol {
                            flex-grow: 1;
                            padding: 10px;
                            font-size: 16px;
                            border: 1px solid #ddd;
                            border-radius: 4px;
                        }
                        #getPrice {
                            padding: 10px 20px;
                            background-color: #3498db;
                            color: white;
                            border: none;
                            border-radius: 4px;
                            cursor: pointer;
                            transition: background-color 0.3s;
                        }
                        #getPrice:hover {
                            background-color: #2980b9;
                        }
                        #result {
                            background-color: #ecf0f1;
                            padding: 20px;
                            border-radius: 4px;
                            margin-top: 20px;
                        }
                        .stock-info {
                            display: flex;
                            justify-content: space-between;
                            margin-bottom: 10px;
                        }
                        .stock-info span {
                            font-weight: bold;
                        }
                        #chart {
                            width: 100%;
                            height: 300px;
                            margin-top: 20px;
                        }
                        @media (max-width: 600px) {
                            #stockForm {
                                flex-direction: column;
                            }
                            #getPrice {
                                width: 100%;
                            }
                        }
                    </style>
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                </head>
                <body>
                    <div class="container">
                        <h1>Stock Tracker</h1>
                        <form id="stockForm">
                            <input type="text" id="symbol" placeholder="Enter stock symbol (e.g., AAPL, GOOGL)" required>
                            <button type="submit" id="getPrice">Get Stock Info</button>
                        </form>
                        <div id="result"></div>
                        <canvas id="chart"></canvas>
                    </div>
                    <script>
                        let chart;
                        document.getElementById('stockForm').addEventListener('submit', function(e) {
                            e.preventDefault();
                            var symbol = document.getElementById('symbol').value.toUpperCase();
                            fetch(`/stock?symbol=${symbol}`)
                                .then(response => response.json())
                                .then(data => {
                                    if (data.error) {
                                        document.getElementById('result').innerHTML = `<p style="color: red;">${data.error}</p>`;
                                        return;
                                    }
                                    let resultHtml = `
                                        <div class="stock-info"><div>Symbol:</div><span>${data.symbol}</span></div>
                                        <div class="stock-info"><div>Company Name:</div><span>${data.name}</span></div>
                                        <div class="stock-info"><div>Current Price:</div><span>$${data.price.toFixed(2)}</span></div>
                                        <div class="stock-info"><div>Change:</div><span style="color: ${data.change >= 0 ? 'green' : 'red'}">${data.change >= 0 ? '+' : ''}${data.change.toFixed(2)}%</span></div>
                                        <div class="stock-info"><div>Volume:</div><span>${data.volume.toLocaleString()}</span></div>
                                    `;
                                    document.getElementById('result').innerHTML = resultHtml;

                                    if (chart) {
                                        chart.destroy();
                                    }
                                    var ctx = document.getElementById('chart').getContext('2d');
                                    chart = new Chart(ctx, {
                                        type: 'line',
                                        data: {
                                            labels: data.historicalData.map(d => d.date),
                                            datasets: [{
                                                label: 'Stock Price',
                                                data: data.historicalData.map(d => d.price),
                                                borderColor: 'rgb(75, 192, 192)',
                                                tension: 0.1
                                            }]
                                        },
                                        options: {
                                            responsive: true,
                                            scales: {
                                                y: {
                                                    beginAtZero: false
                                                }
                                            }
                                        }
                                    });
                                })
                                .catch(error => {
                                    console.error('Error:', error);
                                    document.getElementById('result').innerHTML = '<p style="color: red;">An error occurred. Please try again.</p>';
                                });
                        });
                    </script>
                </body>
                </html>
            """;

            if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().equals("/stock")) {
                Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
                String symbol = params.get("symbol");
                Stock stock = stockDatabase.get(symbol);

                String jsonResponse;
                if (stock != null) { List<String> historicalDataJson = new ArrayList<>();
                    for (HistoricalData historicalData : stock.historicalData) {
                        historicalDataJson.add(String.format("{\"date\":\"%s\",\"price\":%.2f}", historicalData.date, historicalData.price));
                    }
                    jsonResponse = String.format(
                        "{\"symbol\":\"%s\",\"name\":\"%s\",\"price\":%.2f,\"change\":%.2f,\"volume\":%d,\"historicalData\":[%s]}",
                        stock.symbol, stock.name, stock.price, stock.change, stock.volume,
                        String.join(",", historicalDataJson)
                    );
                } else {
                    jsonResponse = "{\"error\":\"Stock symbol not found\"}";
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            } else {
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    private static void initializeStockDatabase() {
        Random random = new Random();
        stockDatabase.put("AAPL", new Stock("AAPL", "Apple Inc.", 150.0, 2.5, 1000000, generateHistoricalData(random, 30)));
        stockDatabase.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", 2500.0, 1.2, 500000, generateHistoricalData(random, 30)));
    }

    private static HistoricalData[] generateHistoricalData(Random random, int days) {
        HistoricalData[] historicalData = new HistoricalData[days];
        for (int i = 0; i < days; i++) {
            double price = 100.0 + random.nextDouble() * 20.0;
            historicalData[i] = new HistoricalData(String.format("2023-01-%02d", i + 1), price);
        }
        return historicalData;
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                params.put(keyValue[0], keyValue[1]);
            }
        }
        return params;
    }

    static class Stock {
        String symbol;
        String name;
        double price;
        double change;
        int volume;
        HistoricalData[] historicalData;

        Stock(String symbol, String name, double price, double change, int volume, HistoricalData[] historicalData) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
            this.change = change;
            this.volume = volume;
            this.historicalData = historicalData;
        }
    }

    static class HistoricalData {
        String date;
        double price;

        HistoricalData(String date, double price) {
            this.date = date;
            this.price = price;
        }
    }
}
