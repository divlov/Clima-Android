    package com.londonappbrewery.climapm;

    import android.Manifest;
    import android.annotation.SuppressLint;
    import android.content.Context;
    import android.content.Intent;
    import android.content.pm.PackageManager;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.os.Build;
    import android.os.Bundle;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;

    import android.util.Log;
    import android.view.View;
    import android.widget.ImageButton;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.material.snackbar.Snackbar;
    import com.loopj.android.http.AsyncHttpClient;
    import com.loopj.android.http.JsonHttpResponseHandler;
    import com.loopj.android.http.RequestParams;

    import org.json.JSONObject;

    import java.lang.reflect.Field;

    import cz.msebera.android.httpclient.Header;


    public class WeatherController extends AppCompatActivity {

        // Constants:
        final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
        // App ID to use OpenWeather data
        final String APP_ID = "a3628af7db30f675c26ad6c45bbff0c5";
        // Time between location updates (5000 milliseconds or 5 seconds)
        final long MIN_TIME = 5000;
        // Distance between location updates (1000m or 1km)
        final float MIN_DISTANCE = 1000;

        final int LOCATION_REQUEST_CODE = 5;
        final String TAG = "ClimaDebug";

        // TODO: Set LOCATION_PROVIDER here:
        final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


        // Member Variables:
        TextView mCityLabel;
        ImageView mWeatherImage;
        TextView mTemperatureLabel;

        // TODO: Declare a LocationManager and a LocationListener here:
        LocationManager locationManager;
        LocationListener locationListener;
        WeatherDataModel weatherDataModel;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.weather_controller_layout);

            // Linking the elements in the layout to Java code
            mCityLabel = (TextView) findViewById(R.id.locationTV);
            mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
            mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
            ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


            // TODO: Add an OnClickListener to the changeCityButton here:
            changeCityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(WeatherController.this, ChangeCityController.class);
                    startActivity(intent);
                }
            });
        }


        // TODO: Add onResume() here:

        @Override
        protected void onResume() {
            super.onResume();
            Log.d(TAG, "In onResume");
            Log.d(TAG, "Getting Location");
            Intent myIntent=getIntent();
            String city=myIntent.getStringExtra("city");
            if(city!=null)
                getWeatherForNewCity(city);
            else
                getWeatherForCurrentLocation();
        }


        // TODO: Add getWeatherForNewCity(String city) here:
        private void getWeatherForNewCity(String city){
            RequestParams params=new RequestParams();
            params.put("q",city);
            params.put("appid",APP_ID);
            letsDoSomeNetworking(params);
        }

        // TODO: Add getWeatherForCurrentLocation() here:
        private void getWeatherForCurrentLocation() {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Log.d(TAG, "OnProviderDisabled callback received");
                }

                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d(TAG, "OnLocationChanged callback received");
                    String longitude=String.valueOf(location.getLongitude());
                    String latitude=String.valueOf(location.getLatitude());
                    RequestParams params=new RequestParams();
                    params.put("lat",latitude);
                    params.put("lon",longitude);
                    params.put("appid",APP_ID);
                    letsDoSomeNetworking(params);
                }
            };
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
            } else
                requestPermission();

        }


        // TODO: Add letsDoSomeNetworking(RequestParams params) here:
        private void letsDoSomeNetworking(RequestParams params){
            AsyncHttpClient client=new AsyncHttpClient();
            client.get(WEATHER_URL, params,new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d(TAG,"Success! JSON:"+response.toString());
                    weatherDataModel=WeatherDataModel.fromJson(response);
                    updateUI(weatherDataModel);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.e(TAG,"Fail "+throwable.getMessage());
                    Log.e(TAG,"Status Code: "+statusCode);
                    Toast.makeText(WeatherController.this,"Unable to fetch Weather data",Toast.LENGTH_SHORT).show();
                }
            });

        }

        // TODO: Add updateUI() here:
        private void updateUI(WeatherDataModel weatherModel){
            mCityLabel.setText(weatherModel.getmCity());
            mTemperatureLabel.setText(weatherModel.getmTemperature());

            /* slow way
            int resID=getResources().getIdentifier(weatherModel.getmIconName(),"drawable",getPackageName()); */

            //faster
            int drawableId=R.drawable.dunno;
            try {
                Class res = R.drawable.class;
                Field field = res.getField(weatherModel.getmIconName());
                drawableId = field.getInt(field);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            mWeatherImage.setImageResource(drawableId);
        }

        // TODO: Add onPause() here:

        @Override
        protected void onPause() {
            if(locationManager!=null){
                locationManager.removeUpdates(locationListener);
            }
            super.onPause();
        }

        private void requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(findViewById(R.id.content), "Allow permission to show weather based on your location", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Allow", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(WeatherController.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                                }
                            }).show();
                } else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == LOCATION_REQUEST_CODE) {
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
                }
            } else
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
