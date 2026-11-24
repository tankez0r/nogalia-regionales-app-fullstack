package com.nogalina.regionales;

import android.app.Application;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Pedido<T> {
    public final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    public final MutableLiveData<T> datos = new MutableLiveData<>();
    public final MutableLiveData<String> error = new MutableLiveData<>();
    public final MutableLiveData<Map<String, String>> detalle = new MutableLiveData<>();

    private final Application app;
    private Call<T> enCurso;

    public Pedido(Application app) {
        this.app = app;
    }

    public void primeraVez(Call<T> llamada) {
        if (enCurso == null) {
            empezar(llamada);
        }
    }

    public void enviar(Call<T> llamada) {
        if (Boolean.TRUE.equals(cargando.getValue())) {
            return;
        }
        empezar(llamada);
    }

    public void recargar(Call<T> llamada) {
        if (enCurso != null) {
            enCurso.cancel();
        }
        empezar(llamada);
    }

    public void limpiar() {
        datos.setValue(null);
        error.setValue(null);
    }

    private void empezar(Call<T> llamada) {
        enCurso = llamada;
        cargando.setValue(true);
        error.setValue(null);
        detalle.setValue(null);

        llamada.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> respuesta) {
                if (call != enCurso) {
                    return;
                }
                cargando.setValue(false);
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    datos.setValue(respuesta.body());
                } else {
                    leerError(respuesta);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                if (call != enCurso) {
                    return;
                }
                cargando.setValue(false);
                error.setValue(app.getString(t instanceof IOException
                        ? R.string.error_conexion : R.string.error_respuesta));
            }
        });
    }

    private void leerError(Response<T> respuesta) {
        String texto = null;
        try (ResponseBody cuerpo = respuesta.errorBody()) {
            ErrorApi e = Api.GSON.fromJson(cuerpo.charStream(), ErrorApi.class);
            if (e.detalle != null) {
                detalle.setValue(e.detalle);
                texto = TextUtils.join(". ", e.detalle.values());
            } else {
                texto = e.error;
            }
        } catch (Exception ignorado) {
        }
        error.setValue(texto != null ? texto : app.getString(R.string.error_http, respuesta.code()));
    }

    static class ErrorApi {
        String error;
        Map<String, String> detalle;
    }
}
