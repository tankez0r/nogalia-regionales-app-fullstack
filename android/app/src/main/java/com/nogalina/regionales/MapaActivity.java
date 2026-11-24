package com.nogalina.regionales;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;

public class MapaActivity extends AppCompatActivity {

    private MapView mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IConfigurationProvider configuracion = Configuration.getInstance();
        configuracion.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        configuracion.setUserAgentValue(getPackageName());
        configuracion.setOsmdroidBasePath(getCacheDir());
        configuracion.setOsmdroidTileCache(new File(getCacheDir(), "mapas"));

        setContentView(R.layout.activity_mapa);
        setTitle(R.string.donde_estamos);

        GeoPoint famatina = new GeoPoint(-28.9167, -67.5167);
        mapa = findViewById(R.id.mapa);
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);
        mapa.getController().setZoom(14.0);
        mapa.getController().setCenter(famatina);
        mapa.getOverlays().add(new CopyrightOverlay(this));

        Marker marcador = new Marker(mapa);
        marcador.setPosition(famatina);
        marcador.setTitle(getString(R.string.app_name));
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapa.getOverlays().add(marcador);
        marcador.showInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapa.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapa.onPause();
    }
}
