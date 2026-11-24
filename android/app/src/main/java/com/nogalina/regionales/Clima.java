package com.nogalina.regionales;

import com.google.gson.annotations.SerializedName;

public class Clima {
    public Actual current;
    public Diario daily;

    public static class Actual {
        @SerializedName("temperature_2m")
        public double temperatura;
        @SerializedName("weather_code")
        public int codigo;
        @SerializedName("wind_speed_10m")
        public double viento;
        @SerializedName("is_day")
        public int esDeDia;
    }

    public static class Diario {
        @SerializedName("temperature_2m_max")
        public double[] maxima;
        @SerializedName("temperature_2m_min")
        public double[] minima;
    }
}
