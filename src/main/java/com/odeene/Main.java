package com.odeene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main {
    static String diaActual;
    static int[] indices = {6, 14, 22};

    public static void main(String[] args) throws SQLException {                                                //MAIN///////////////////////////////////////
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

        for (City city : cities) {
            // Crear y ejecutar la petición HTTP
            Request request = new Request.Builder()
            .url(url +city.getLongitude()+ "," + city.getLatitude() + "&variables=sky_state,temperature,precipitation_amount,wind,relative_humidity,cloud_area_fraction&API_KEY=" + API_KEY)
            .build();

            //System.out.println("\n" + city.getName());

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
        Scanner scInt = new Scanner(System.in);

        do {
            System.out.println("\nMENU");
            System.out.println("1. Guardar pronostico en la BD (Escoger al ejecutar el programa por primera vez)");
            System.out.println("2.Mostrar ciudades disponibles");
            System.out.println("3. Salir");
            opcion = scInt.nextInt();
            switch(opcion){
                case 1:
                    writeWeatherDataToDB(weatherDatas, cities);
                    break;
                case 2:
                    boolean bucle2 = true;
                    do {
                        int num = 0;
                        System.out.println("Escoge la ciudad sobre la que quieres consultar/modificar datos");
                        for (City ciudad: cities) {
                            num++;
                            System.out.println(num + ") " + ciudad.getName());
                        }
                        int ciudad = scInt.nextInt(); //Indice para luego usar al recorrer el array de ciudades
                        System.out.println("\n1. Consultar datos");
                        System.out.println("2. Modificar datos");
                        System.out.println("3. Volver");

                        opcion = scInt.nextInt();
                        //Lista de parametros del tiempo para escoger
                        switch (opcion) {
                            //Consultar
                            case 1:
                                int contador = 0;
                                for (String dato : listaDeParametros) {
                                    contador++;
                                    System.out.println(contador + ") " + dato);
                                }
                                int parametro = scInt.nextInt();


                                break;
                            // Modificar
                            case 2:
                                break;
                            // Volver
                            case 3:
                                bucle2 = false;
                                break;
                        }
                    }while(bucle2);
                    break;
                case 3:
                    bucle = false;
                    break;
            }
        }while(bucle);

    }

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
            //System.out.println("\nDia: " + diaActual);

            // Obtener los valores de estado del cielo (sky_state)
            List<JsonNode> skyStateNode = firstDay.path("variables").get(0).findValues("value");
            if (skyStateNode != null) {
                //System.out.println("Estado del cielo:");
                /*for (JsonNode valueNode : skyStateNode) {
                    System.out.println(valueNode.asText());
                    sky_state.add(valueNode.asText());
                }*/
                for (int index : indices) {
                    if (index < skyStateNode.size()) { // Verificar que el índice no exceda el tamaño del array
                        JsonNode valueNode = skyStateNode.get(index);
                        //System.out.println(valueNode.asText());
                        sky_state.add(valueNode.asText());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre el estado del cielo.");
            }

            // Obtener los valores de temperatura
            List<JsonNode> temperatureNode = firstDay.path("variables").get(1).findValues("value");
            if (temperatureNode != null) {
                //System.out.println("\nTemperatura:");
                /*for (JsonNode valueNode : temperatureNode) {
                    System.out.println(valueNode.asDouble() + " C");
                    temperature.add(valueNode.asDouble());
                }*/
                for (int index : indices) {
                    if (index < temperatureNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = temperatureNode.get(index);
                        //System.out.println(valueNode.asDouble() + " C");
                        temperature.add(valueNode.asDouble());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre la temperatura.");
            }

            // Obtener los valores de lluvias
            List<JsonNode> precipitationAmountNode = firstDay.path("variables").get(2).findValues("value");
            if (precipitationAmountNode != null) {
                //System.out.println("\nProbabilidad de lluvias:");
                /*for (JsonNode valueNode : precipitationAmountNode) {
                    System.out.println(valueNode.asInt() + "%");
                    precipitation_amount.add(valueNode.asInt());
                }*/
                for (int index : indices) {
                    if (index < precipitationAmountNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = precipitationAmountNode.get(index);
                        //System.out.println(valueNode.asInt() + "%");
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
                //System.out.println("\nViento:");
                /*for (int i = 0; i < windNode.size(); i++) {
                    System.out.println(windNode.get(i).asDouble() + "kmh");
                    System.out.println(windDirectionNode.get(i).asDouble() + "->");
                    wind.add(new Wind(windNode.get(i).asDouble(), windDirectionNode.get(i).asDouble()));
                }*/
                for (int index : indices) {
                    if (index < windNode.size() && index < windDirectionNode.size()) { // Evitar IndexOutOfBoundsException
                        double windSpeed = windNode.get(index).asDouble();
                        double windDirection = windDirectionNode.get(index).asDouble();

                        //System.out.println(windSpeed + " km/h");
                        //System.out.println(windDirection + " ->");

                        wind.add(new Wind(windSpeed, windDirection));
                    }
                }
            } else {
                System.out.println("No se encontró información sobre el viento.");
            }

            // Obtener los valores de humedad
            List<JsonNode> relativeHumidityNode = firstDay.path("variables").get(4).findValues("value");
            if (relativeHumidityNode != null) {
                //System.out.println("\nHumedad:");
                /*for (JsonNode valueNode : relativeHumidityNode) {
                    System.out.println(valueNode.asDouble() + "%");
                    relative_humidity.add(valueNode.asDouble());
                }*/
                for (int index : indices) {
                    if (index < relativeHumidityNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = relativeHumidityNode.get(index);
                        //System.out.println(valueNode.asDouble() + "%");
                        relative_humidity.add(valueNode.asDouble());
                    }
                }
            } else {
                System.out.println("No se encontró información sobre la humedad de la zona.");
            }

            // Obtener los valores de cobertura nubosa
            List<JsonNode> cloudAreaFractionNode = firstDay.path("variables").get(5).findValues("value");
            if (cloudAreaFractionNode != null) {
                //System.out.println("\nCobertura nubosa:");
                /*for (JsonNode valueNode : cloudAreaFractionNode) {
                    System.out.println(valueNode.asDouble() + "%");
                    cloud_area_fraction.add(valueNode.asDouble());
                }*/
                for (int index : indices) {
                    if (index < cloudAreaFractionNode.size()) { // Evitar IndexOutOfBoundsException
                        JsonNode valueNode = cloudAreaFractionNode.get(index);
                        //System.out.println(valueNode.asDouble() + "%");
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
                    DBConnection.insertarDatos(cities[i].getName(), diaActual, franjaHoraria, cielo, temperatura, lluvias, viento, humedad, nubosidad);
                }
            } else {
                System.out.println("Datos ya existentes para " + cities[i].getName() + " en la fecha " + diaActual);
            }
        }
    }
}