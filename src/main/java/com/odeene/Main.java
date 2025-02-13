package com.odeene;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main {
    static String diaActual;
    static int[] indices = {6, 14, 22}; //Indices para coger las franjas, representan las 7, 15, y 23 (MAÑANA,TARDE,NOCHE)

    public static void main(String[] args) throws SQLException {
        DBConnection.conectarBD(); // Conectamos con la base

        List<WeatherData> weatherDatas = new ArrayList<>();
        // URL de la API
        String url = "https://servizos.meteogalicia.gal/apiv4/getNumericForecastInfo?coords=";
        String API_KEY = "Asf85bkF56Ae0PJlCCOLS1Ci47mY4CTWjafPV4erU3MIoLzM1gD38O8FunARHg4U";

        ArrayList<String> listaDeParametros = new ArrayList<>();

        listaDeParametros.add("Cielo");
        listaDeParametros.add("Temperatura");
        listaDeParametros.add("Lluvias");
        listaDeParametros.add("Viento");
        listaDeParametros.add("Humedad");
        listaDeParametros.add("Nubosidad");

        String[] tipoDeDato = {"estado_del_cielo", "temperatura", "probabilidad_de_lluvias", "viento", "humedad", "cobertura_nubosa"};

        // Cliente HTTP
        OkHttpClient client = new OkHttpClient();
        City[] cities = {
            new City("A Coruna", 43.3623, -8.4115),
            new City("Lugo", 43.0121, -7.5558),
            new City("Ourense", 42.335, -7.8639),
            new City("Pontevedra", 42.4333, -8.6443),
            new City("Vigo", 42.2406, -8.7207),
            new City("Santiago", 42.8805, -8.5457),
            new City("Ferrol", 43.4831, -8.2369)
        }; 

        // Peticion a la API para obtener los datos meteorológico
        for (City city : cities) {
            // Crear y ejecutar la petición HTTP
            Request request = new Request.Builder()
            .url(url +city.getLongitude()+ "," + city.getLatitude() + "&variables=sky_state,temperature,precipitation_amount,wind,relative_humidity,cloud_area_fraction&API_KEY=" + API_KEY)
            .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // Procesar la respuesta JSON
                    String jsonResponse = response.body().string();
                    processForecastData(jsonResponse, weatherDatas);
                } else {
                    System.err.println("Error en la petición HTTP: " + response.code());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int opcion;
        boolean bucle = true;
        // Hay un escaner para enteros y otro para strings para evotar errores de buffer
        Scanner scInt = new Scanner(System.in);
        Scanner sc = new Scanner(System.in);

        // Bucle del menú de la interfaz
        do {
            System.out.println("\nMENU");
            System.out.println("1. Guardar pronostico en la BD (Escoger al ejecutar el programa por primera vez)");
            System.out.println("2. Mostrar ciudades disponibles");
            System.out.println("3. Salir");

            System.out.print("\n-> ");
            opcion = scInt.nextInt();
            switch(opcion){
                case 1:
                    // Método para guardar las previsiones en la BD
                    writeWeatherDataToDB(weatherDatas, cities);
                    break;
                case 2:
                    boolean bucle2 = true;
                    do {
                        int num = 0;
                        System.out.println("\nEscoge la ciudad sobre la que quieres consultar/modificar datos");
                        for (City ciudad: cities) {
                            num++;
                            System.out.println(num + ") " + ciudad.getName());
                        }
                        System.out.println("\n8) Volver");
                        System.out.print("\n-> ");
                        int ciudadInt = scInt.nextInt(); //Índice para luego usar al recorrer el array de ciudades
                        if(ciudadInt == 8){
                            break;
                        }
                        System.out.println("\n1. Consultar datos");
                        System.out.println("2. Modificar datos");
                        System.out.println("3. Volver atrás");

                        System.out.print("\n-> ");
                        opcion = scInt.nextInt();
                        switch (opcion) {
                            //Consultar
                            case 1:
                                int contador = 0;
                                //Lista de parametros del tiempo para escoger cual mostrar
                                for (String dato : listaDeParametros) {
                                    contador++;
                                    System.out.println(contador + ") " + dato);
                                }
                                System.out.print("\n-> ");
                                int parametro = scInt.nextInt(); // Indice del parametro
                                ArrayList<String> resultadoConsulta = DBConnection.consultaDePrevisionPorCiudadYFranja(cities[ciudadInt-1].getName(), tipoDeDato[parametro-1]);
                                System.out.println(imprimirFormateado(resultadoConsulta));
                                break;
                            // Modificar
                            case 2:
                                contador = 0;
                                //Lista de parametros del tiempo para escoger cual modificar
                                for (String dato : listaDeParametros) {
                                    contador++;
                                    System.out.println(contador + ") " + dato);
                                }
                                System.out.print("\n-> ");
                                parametro = scInt.nextInt(); // Indice del parámetro a modificar

                                // Consultar y mostrar datos actuales
                                resultadoConsulta = DBConnection.consultaDePrevisionPorCiudadYFranja(cities[ciudadInt-1].getName(), tipoDeDato[parametro-1]);
                                System.out.println("Datos actuales:");
                                for (String fila : resultadoConsulta) {
                                    System.out.println(fila);
                                }

                                boolean bucleSQL = true;
                                String nuevoValor;
                                String franja;

                                do {
                                    // Escoger la franja y el dato de la previsión a modificar
                                    System.out.println("Introduce la franja(MAÑANA, TARDE, NOCHE): ");
                                    System.out.print("\n-> ");
                                    franja = sc.nextLine();
                                    System.out.println("Introduce el nuevo valor para " + listaDeParametros.get(parametro - 1) + " o escribe 'cancelar' para salir:");
                                    System.out.print("\n-> ");
                                    scInt.nextLine(); // Limpiar buffer
                                    System.out.print("\n-> ");
                                    nuevoValor = sc.nextLine();


                                    // Validar si hay palabras SQL prohibidas
                                    if (contienePalabrasSQL(nuevoValor) || contienePalabrasSQL(franja)) {
                                        System.out.println("¡Error! Entrada inválida detectada.");
                                    } else {
                                        System.out.println("Valor y franja aceptados.");
                                        bucleSQL = false;
                                    }
                                }while(bucleSQL);

                                if (!nuevoValor.equalsIgnoreCase("cancelar")) {
                                    DBConnection.modificarDatodePrevision(cities[ciudadInt-1].getName(), tipoDeDato[parametro-1], franja, nuevoValor);
                                    System.out.println("Dato actualizado correctamente.");
                                } else {
                                    System.out.println("Modificación cancelada.");
                                }
                                break;

                            // Volver
                            case 3:
                                bucle2 = false;
                                break;
                        }
                    }while(bucle2);
                    break;
                case 3:
                    // Salimos del bucle y del programa
                    bucle = false;
                    break;
            }
        }while(bucle);

    }

    /**
     * Método para procesar los datos en formato JSON de al prevision
     * @param jsonResponse
     * @param weatherDatas
     */
    private static void processForecastData(String jsonResponse, List<WeatherData> weatherDatas) {          //PROCESAR DATOS/////////////////////////////////////////////
        try {
            List<String> sky_state = new ArrayList<>();
            List<Double> temperature = new ArrayList<>();
            List<Integer> precipitation_amount = new ArrayList<>();
            List<Wind> wind = new ArrayList<>();
            List<Double> relative_humidity = new ArrayList<>();
            List<Double> cloud_area_fraction = new ArrayList<>();
            // Parsear el JSON usando ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            // Obtener el primer día del pronóstico
            JsonNode firstDay = root.at("/features/0/properties/days/1");
            if (firstDay.isMissingNode()) {
                System.out.println("No se encontró información del primer día.");
                return;
            }
            diaActual = firstDay.findValue("timeInstant").asText().split("T")[0];

            // Obtener los valores de estado del cielo (sky_state)
            List<JsonNode> skyStateNode = firstDay.path("variables").get(0).findValues("value");
            if (skyStateNode != null) {
                for (int index : indices) {
                    if (index < skyStateNode.size()) { // Verificar que el índice no exceda el tamaño del array
                        JsonNode valueNode = skyStateNode.get(index);
                        sky_state.add(valueNode.asText());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre el estado del cielo.");
            }

            // Obtener los valores de temperatura
            List<JsonNode> temperatureNode = firstDay.path("variables").get(1).findValues("value");
            if (temperatureNode != null) {
                for (int index : indices) {
                    if (index < temperatureNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = temperatureNode.get(index);
                        temperature.add(valueNode.asDouble());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre la temperatura.");
            }

            // Obtener los valores de lluvias
            List<JsonNode> precipitationAmountNode = firstDay.path("variables").get(2).findValues("value");
            if (precipitationAmountNode != null) {
                for (int index : indices) {
                    if (index < precipitationAmountNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = precipitationAmountNode.get(index);
                        precipitation_amount.add(valueNode.asInt());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre las probablidades de lluvias.");
            }

            // Obtener los valores del viento
            List<JsonNode> windNode = firstDay.path("variables").get(3).findValues("moduleValue");
            List<JsonNode> windDirectionNode = firstDay.path("variables").get(3).findValues("directionValue");
            if (windNode != null && windDirectionNode != null) {
                for (int index : indices) {
                    if (index < windNode.size() && index < windDirectionNode.size()) { // Evitar IndexOutOfBoundsException
                        double windSpeed = windNode.get(index).asDouble();
                        double windDirection = windDirectionNode.get(index).asDouble();
                        wind.add(new Wind(windSpeed, windDirection));
                    }
                }
            } else {
                System.out.println("No se encontró información sobre el viento.");
            }

            // Obtener los valores de humedad
            List<JsonNode> relativeHumidityNode = firstDay.path("variables").get(4).findValues("value");
            if (relativeHumidityNode != null) {
                for (int index : indices) {
                    if (index < relativeHumidityNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = relativeHumidityNode.get(index);
                        relative_humidity.add(valueNode.asDouble());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre la humedad de la zona.");
            }

            // Obtener los valores de cobertura nubosa
            List<JsonNode> cloudAreaFractionNode = firstDay.path("variables").get(5).findValues("value");
            if (cloudAreaFractionNode != null) {
                for (int index : indices) {
                    if (index < cloudAreaFractionNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = cloudAreaFractionNode.get(index);
                        cloud_area_fraction.add(valueNode.asDouble());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre la cobertura nubosa de la zona.");
            }
            weatherDatas.add(new WeatherData(sky_state, temperature, precipitation_amount, wind, relative_humidity, cloud_area_fraction));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Guardar los datos procesados del JSON en la BD sobre las previsiones meteorológicas de cada ciudad
     * @param weatherDataArray Array con los elementos de la previsión
     * @param cities Array de ciudades
     * @throws SQLException
     */
    public static void writeWeatherDataToDB(List<WeatherData> weatherDataArray, City[] cities) throws SQLException {
        for (int i = 0; i < cities.length; i++) {
            // Verificar si ya existen datos para la ciudad y el día actual
            boolean datosExistentes = DBConnection.existenDatosParaFechaYCiudad(diaActual, cities[i].getName());
            if (!datosExistentes) {
                // Recorremos las franjas horarias para la ciudad actual
                for (int j = 0; j < 3; j++) {
                    String cielo = weatherDataArray.get(0).getSky_state().get(j);
                    String temperatura = weatherDataArray.get(1).getTemperature().get(j).toString();
                    String lluvias = weatherDataArray.get(2).getPrecipitation_amount().get(j).toString();
                    String viento = weatherDataArray.get(3).getWind().get(j).toString();
                    String humedad = weatherDataArray.get(4).getRelative_humidity().get(j).toString();
                    String nubosidad = weatherDataArray.get(5).getCloud_area_fraction().get(j).toString();
                    // Determina la franja horaria en función del índice j
                    String franjaHoraria = "";
                    switch (j) {
                        case 0:
                            franjaHoraria = "MAÑANA";
                            break;
                        case 1:
                            franjaHoraria = "TARDE";
                            break;
                        case 2:
                            franjaHoraria = "NOCHE";
                            break;
                    }
                    // Insertar los datos para la ciudad y la franja horaria correspondiente
                    DBConnection.insertarDatos(cities[i].getName(),
                            diaActual,
                            franjaHoraria,
                            cielo,
                            temperatura,
                            lluvias,
                            viento,
                            humedad,
                            nubosidad);
                }
            } else {
                System.out.println("Datos ya existentes para " + cities[i].getName() + " en la fecha " + diaActual);
            }
        }
    }

    /**
     * Método para mostrar la informacion con un formato facil de interpretar y ordenado
     * @param array que contine los datos de la previsión
     * @return
     */
    public static String imprimirFormateado(ArrayList<String> array){
        String datosFormateados = "";
        for (String dato : array) {
            datosFormateados += dato + "\n";
        }
        return datosFormateados;
    }

    /**
     * Método para comprobar que no haya SQL injections en las entradas por terminal al epdir datos
     * @param texto
     * @return
     */
    public static boolean contienePalabrasSQL(String texto) {
        String textoLower = texto.toLowerCase();
        return textoLower.contains("update") || textoLower.contains("insert") || textoLower.contains("delete");
    }
}