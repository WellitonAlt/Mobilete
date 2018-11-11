package br.com.mobilete

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import br.com.mobilete.AppConstants.REQUEST_LOCATION
import br.com.mobilete.AppConstants.REQUEST_PLACE_PICKER
import br.com.mobilete.AppConstants.TAG_LOC
import br.com.mobilete.AppConstants.TAG_MAP
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.locationManager
import java.io.IOException

class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_LOCALIZACAO: String = "Localizacao"
        const val EXTRA_ENDERECO: String = "Endereco"
        const val EXTRA_LUGAR: String = "Lugar"
    }

    private lateinit var mapFragment : SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var enderecoFinal: String = ""
    private var lugar: String = ""
    private var localizacao: String = ""

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)


        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            if (locationPermission())
                mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

            //Click no Mapa
            googleMap.setOnMapClickListener(
                object : GoogleMap.OnMapClickListener {
                    override fun onMapClick(p0: LatLng?) {
                        Log.d(TAG_MAP, "Click $p0 - p0!!")
                        localizacao = p0.toString()
                        lugar = ""
                        alerta(getEndereco(p0!!))
                    }
                })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mapa, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId){
            R.id.menuProcurar -> carregaLugar()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_LOCATION && (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAG_LOC, "Permissão Negada")
        }
    }

    private fun alerta(endereco: String){
        val builder = AlertDialog.Builder(this@MapaActivity)
        builder.setTitle("Deseja adicionar esse local?")
        builder.setMessage(endereco)
        builder.setPositiveButton("Sim"){dialog, which ->
            goToCadAnucio()
        }
        builder.setNegativeButton("Não"){dialog,which ->
            Log.d(TAG_MAP, "Nao")
        }
        builder.create().show()
    }

    private fun colocaMarkerNoMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)
        mMap.addMarker(markerOptions)
    }

    private fun getEndereco(latLng: LatLng): String {
        val geocoder = Geocoder(this)
        val enderecos: List<Address>?
        val endereco: Address?
        enderecoFinal = ""
        try {
            enderecos = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != enderecos && !enderecos.isEmpty()) {
                endereco = enderecos[0]
                for (i in 0..endereco.maxAddressLineIndex) {
                    enderecoFinal += if (i == 0) endereco.getAddressLine(i) else "\n" + endereco.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG_MAP, e.localizedMessage)
        }
        if (enderecoFinal == "")
            enderecoFinal = "Endereço indiponivel no momento $latLng"
        return enderecoFinal
    }

    private fun carregaLugar() {
        val builder = PlacePicker.IntentBuilder()

        try {
            startActivityForResult(builder.build(this@MapaActivity), REQUEST_PLACE_PICKER)
        } catch (e: GooglePlayServicesRepairableException) {
            Log.d(TAG_LOC, e.printStackTrace().toString())
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.d(TAG_LOC, e.printStackTrace().toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                localizacao = PlacePicker.getPlace(this, data).latLng.toString()
                lugar = PlacePicker.getPlace(this, data).name.toString()
                enderecoFinal = PlacePicker.getPlace(this, data).address.toString()
                goToCadAnucio()
            }
        }
    }

    private fun goToCadAnucio(){
        val goToCadAnuncio = Intent(this, CadastroAnuncioActivity::class.java)
        val extras = Bundle()
        extras.putString(EXTRA_LOCALIZACAO, localizacao)
        extras.putString(EXTRA_ENDERECO, enderecoFinal)
        extras.putString(EXTRA_LUGAR, lugar)
        goToCadAnuncio.putExtras(extras)
        setResult(Activity.RESULT_OK, goToCadAnuncio)
        finish()
    }

    private fun locationPermission() : Boolean {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), AppConstants.REQUEST_LOCATION)
            return false
        }
        return true
    }

}
