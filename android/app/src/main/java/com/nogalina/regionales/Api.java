package com.nogalina.regionales;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public class Api {

    public interface Nogalina {
        @GET("productos.php")
        Call<List<Producto>> productos(@Query("categoria") String categoria, @Query("q") String q,
                                       @Query("bajo_stock") Integer bajoStock);

        @GET("productos.php")
        Call<Producto> producto(@Query("id") int id);

        @POST("productos.php")
        Call<RespuestaProducto> crearProducto(@Body Producto producto);

        @PUT("productos.php")
        Call<RespuestaProducto> editarProducto(@Query("id") int id, @Body Producto producto);

        @DELETE("productos.php")
        Call<Object> borrarProducto(@Query("id") int id);

        @GET("ventas.php")
        Call<List<Venta>> ventas();

        @POST("ventas.php")
        Call<Venta.Respuesta> registrarVenta(@Body Venta.Nueva venta);

        @PUT("ventas.php")
        Call<Venta.Respuesta> anularVenta(@Query("id") int id);

        @GET("notificaciones.php")
        Call<List<Notificacion>> notificaciones(@Query("no_leidas") Integer noLeidas,
                                                @Query("recientes") Integer recientes);

        @PUT("notificaciones.php")
        Call<Object> marcarLeida(@Query("id") int id);
    }

    public interface OpenMeteo {
        @GET("v1/forecast?latitude=-28.9167&longitude=-67.5167"
                + "&current=temperature_2m,weather_code,wind_speed_10m,is_day"
                + "&daily=temperature_2m_max,temperature_2m_min"
                + "&timezone=America%2FArgentina%2FLa_Rioja&forecast_days=1")
        Call<Clima> clima();
    }

    public static class RespuestaProducto {
        public Producto producto;
        public String advertencia;
    }

    static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static final Nogalina nogalina = crear(BuildConfig.API_URL).create(Nogalina.class);
    public static final OpenMeteo openMeteo = crear(BuildConfig.CLIMA_URL).create(OpenMeteo.class);

    private static Retrofit crear(String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build();
    }
}
