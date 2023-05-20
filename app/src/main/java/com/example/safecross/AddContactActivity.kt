package com.example.safecross

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation

import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.util.Random

class AddContactActivity : AppCompatActivity(), IOnLocationListener, OnMapReadyCallback,
    GeoQueryEventListener {

    override fun onStop() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback!!)
        
        super.onStop()
    }
    private var permissionCode = 101
    private  var mMap :GoogleMap?=null
    private lateinit var locationRequest:LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentMarker:Marker?=null
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var dangerousArea:MutableList<LatLng>
    private lateinit var listener:IOnLocationListener
    private lateinit var myCity:DatabaseReference
    private lateinit var lastLocation:android.location.Location
    private var geoQuery: GeoQuery?=null
    private lateinit var geoFire: GeoFire

    lateinit var mapFragment : SupportMapFragment



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        isLocationPermissionGranted()

        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object:PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    buildLocationRequest()
                    buildLocationCallback()
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@AddContactActivity)
                    initArea()
                    settingGeoFire()

                    // add dangerous Firebase
                   // addDangerousToFirebase()

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@AddContactActivity,"You must enable this permission",Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    TODO("Not yet implemented")
                }
            }).check()


    }

    private fun isLocationPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCode -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted()
            }
        }
    }

    private fun addDangerousToFirebase() {
        dangerousArea = ArrayList()
        dangerousArea.add(LatLng(28.47234,77.48807))
        dangerousArea.add(LatLng(28.47926,77.49935))
        dangerousArea.add(LatLng(28.477000,77.49394))

        //submit this list to Firebase
        FirebaseDatabase.getInstance()
            .getReference("DangerousArea")
            .child("MyCity")
            .setValue(dangerousArea)
            .addOnCompleteListener {

                    Toast.makeText(this@AddContactActivity,"Update",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { ex ->
                Toast.makeText(this@AddContactActivity,""+ex.message,Toast.LENGTH_SHORT).show()
            }
    }

    private fun settingGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation")
        geoFire = GeoFire(myLocationRef)
    }

    private fun initArea() {
        myCity = FirebaseDatabase.getInstance()
            .getReference("DangerousArea")
            .child("MyCity")

        listener= this

        //Add realtime change update
        myCity.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Update Dangerous Area List
                val latLnglist = ArrayList<MyLatlng>()

                for (locationSnapShot in dataSnapshot.children){
                    val latLng = locationSnapShot.getValue(MyLatlng::class.java)
                    latLnglist.add(latLng!!)
                }
                listener.onLocationLoadSuccess(latLnglist)

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun buildLocationCallback() {
        locationCallback = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (mMap != null){
                    lastLocation = locationResult.lastLocation!!
                     addUserMarker()
                }
            }
        }
    }

    private fun addUserMarker() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        geoFire.setLocation("You", GeoLocation(lastLocation.latitude,lastLocation.longitude)){
            _,_,->
            if (currentMarker!=null) currentMarker!!.remove()
            currentMarker =
                mMap!!.addMarker(MarkerOptions().position(LatLng(lastLocation.latitude,lastLocation.longitude))
                    .title("You"))!!
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker!!.position,15.0f))
            mMap!!.isMyLocationEnabled = true
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    override fun onLocationLoadSuccess(latLngs: List<MyLatlng>) {
        dangerousArea = ArrayList()
        for (myLatLng in latLngs){
            val convert = LatLng(myLatLng.latitude,myLatLng.longitude)
            dangerousArea!!.add(convert)
        }

        // Now after the dangerous area have the data we will call Map display
        //obtain the support Fragment Manager and get notified when the map is ready to be used

        mapFragment = supportFragmentManager.findFragmentById(R.id.mMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (mMap != null){
            mMap!!.clear()

            //Add Again user marker
             addUserMarker()
            //Add circle of dangerous Area
            addCircleArea()
        }
    }

    private fun addCircleArea() {
        if (geoQuery!=null){
            // remove old listener,image if remove an location in firebase
            // it must be remove listener in GeoFire too
            geoQuery!!.removeGeoQueryEventListener(this@AddContactActivity)
            geoQuery!!.removeAllListeners()
        }

        //Add again
        for (latLng in dangerousArea){
            mMap!!.addCircle(CircleOptions().center(latLng)
                .radius(100.0)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f))

            //create geoQuery when user in Dangerous Location
            geoQuery=geoFire.queryAtLocation(GeoLocation(latLng.latitude,latLng.longitude),0.1)
            geoQuery!!.addGeoQueryEventListener(this@AddContactActivity)
        }
    }

    override fun onLocationLoadFailed(message: String) {
        Toast.makeText(this,""+message,Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        mMap!!.uiSettings.isZoomControlsEnabled = true
        if (fusedLocationProviderClient !=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
                    return
            }
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest,locationCallback!!,Looper.myLooper())
        addCircleArea()

        }



    }

    override fun onKeyEntered(key: String?, location: GeoLocation?) {
        sendNotification("Safe Cross",String.format("is Entered the Dangerous Area",key))
    }

    override fun onKeyExited(key: String?) {
        sendNotification("Safe Cross",String.format("is Leave the Dangerous Area",key))
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {
        sendNotification("Safe Cross",String.format("is Moving in the Dangerous Area",key))
    }

    private fun sendNotification(title: String, content: String) {
        Toast.makeText(this,""+content,Toast.LENGTH_SHORT).show()
        val NotificationChannelId = "edmt_multiple_Location"
        val notificationManger = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NotificationChannelId,
                "MyNotification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            //config
            notificationChannel.description = "ChannelDescription"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManger.createNotificationChannel(notificationChannel)
        }

            val builder = NotificationCompat.Builder(this,NotificationChannelId)
            builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.google_logo)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.twitter_logo))

            val notification = builder.build()
            notificationManger.notify(Random().nextInt(),notification)

    }

    override fun onGeoQueryReady() {
       }

    override fun onGeoQueryError(error: DatabaseError?) {
       Toast.makeText(this,""+error!!.message,Toast.LENGTH_LONG).show()
    }
}

