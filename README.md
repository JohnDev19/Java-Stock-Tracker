# Stock Tracker Server

## Description
Stock Tracker Server is a simple Java-based web application that provides a mock stock tracking interface.

![Screenshot](.jpg)

## Features
- Simple web interface for querying stock information
- Display of current stock price, change percentage, and trading volume
- Historical price chart using Chart.js
- Mock data for demonstration (currently includes AAPL and GOOGL)

## Prerequisites
- Java Development Kit (JDK) 15 or later
- Web browser (Chrome, Firefox, Safari, or Edge recommended)

## Installation and Running
1. Clone the repository or download the `StockTrackerServer.java` file.
2. Open a terminal or command prompt.
3. Navigate to the directory containing `StockTrackerServer.java`.
4. Compile the Java file:
> Run the server:
```java
java StockTrackerServer
```
6. Open a web browser and go to `http://localhost:8080`.

## Usage
1. Enter a stock symbol (e.g., AAPL or GOOGL) in the input field.
2. Click the "Get Stock Info" button.
3. View the displayed stock information and historical price chart.

## Limitations
- This is a demo application with mock data.
- Only AAPL (Apple) and GOOGL (Google) stock symbols are available in the mock database.
- The server runs locally and is not suitable for production environments without modifications.

## Customization
To add more stocks or modify existing ones, edit the `initializeStockDatabase()` method in the `StockTrackerServer.java` file.

## Security Note
This server is intended for local use and demonstration purposes only. It does not implement any security measures and should not be exposed to the public internet.

## License
This project is open-source and available under the MIT [License](License).

## Contributing
Contributions, issues, and feature requests are welcome. Feel free to check issues page if you want to contribute.

## Author
[JohnDev19]

## Acknowledgments
- Chart.js for the interactive stock price charts
- Java HttpServer for the simple web server implementation
