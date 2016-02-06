package com.cycling.assistant;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method; 

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

public class MapDisplayActivity extends Activity 
		implements
		OnMarkerClickListener,
		OnMarkerDragListener{
	//
	List<LatLng> points;
	Polyline polyline = null;
	private Location location;
	private CameraPosition cameraPosition;
	private GoogleMap map;
	private String strResult = "";
	private short asyncTAG = 0;
	private boolean navigate = false;
	private final double EARTH_RADIUS = 6378137.0; 
	
	private double lat = 32.049999;
	private double lng = 118.783330;
	private double endlat = 0.0;
	private double endlng = 0.0;
	
	private int turningCount = 0;
	
	LocationManager locationManager;
	ImageButton local;
	LocationListener locationlistener;
	Marker  positionMarker = null;
	Marker  destinationMarker = null;
	
	private TurningInfo turningInfo;
	
	private enum TurningDirection {
		Turn_left,
		Turn_right,
		Turn_slight_left,
		Turn_slight_right,
		Turn_sharp_left,
		Turn_sharp_right,
		Go_Straight
	}
	
	private ProgressDialog progressdialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapdisplay);
		//local = (ImageButton) findViewById(R.id.local);
		//local.setImageResource(R.drawable.av);
		setUpMapIfNeeded();		
	}

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
	
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
    		map = ((MapFragment) (this.getFragmentManager()
    				.findFragmentById(R.id.map))).getMap();		
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }
    
    private void setUpMap() {
    	//
		
        // Set listeners for marker events.  See the bottom of this class for their behavior.
        map.setOnMarkerClickListener(this);
        map.setOnMarkerDragListener(this);
		
		this.getCurrentLocation();
		
		//MapDisplayActivity.this.setCameraPosition();
		
    }
	
	// Mark current position
	void markCurrentLocation() {
		positionMarker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).
	 			icon(BitmapDescriptorFactory.fromResource(R.drawable.position)));	
	}
	
	// Mark destination
	void markDestinationLocation() {
		if(destinationMarker == null) {
			destinationMarker = map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).
		 			icon(BitmapDescriptorFactory.fromResource(R.drawable.position)).draggable(true));	
		} else {
			destinationMarker = map.addMarker(new MarkerOptions().position(new LatLng(endlat, endlng)).
		 			icon(BitmapDescriptorFactory.fromResource(R.drawable.position)).draggable(true));	
		}
	}

	// Get My Position
	private void getCurrentLocation() {

		Criteria criteria = new Criteria();
		// ACCURACY_FINE 
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		String provider = locationManager.getBestProvider(criteria, true);

		Log.d("provider", provider);
		locationlistener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				//if(positionMarker!=null){			
					//positionMarker.remove();
				//}
				lat = location.getLatitude();
				lng = location.getLongitude();
				if ((positionMarker != null) && (navigate == true)) {
					//
					positionMarker.setPosition(new LatLng(lat, lng));
					MapDisplayActivity.this.setCameraPosition();
					
					//
					if (turningInfo == null) {
						Toast.makeText(getBaseContext(), "Cannot get navigation information</br>please reset navigation", Toast.LENGTH_SHORT).show();
						return;
					}
					if (turningCount < turningInfo.getStepCount()) {
						if ((Math.abs(lat - turningInfo.getLatLng(turningCount).latitude) <= 0.00001) &&
							(Math.abs(lng - turningInfo.getLatLng(turningCount).longitude) <= 0.00001)) {
							Intent command = new Intent("turning command");
							if ((turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_left)
							  ||(turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_slight_left)
							  ||(turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_sharp_left)) {
								  int cmdInfo = -1; //left
								  command.putExtra("cmd", cmdInfo);
							      sendBroadcast(command);
							}
							if ((turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_right)
							  ||(turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_slight_right)
							  ||(turningInfo.getTurningDirection(turningCount) == TurningDirection.Turn_sharp_right)) {
								  int cmdInfo = 1; //right
								  command.putExtra("cmd", cmdInfo);
								  sendBroadcast(command);
							}
							turningCount++;
						}			
					}
				} else {
					MapDisplayActivity.this.markCurrentLocation();
					MapDisplayActivity.this.setCameraPosition();
				}
				
				//MapDisplayActivity.this.markCurrentLocation();
			}

			@Override
			public void onProviderDisabled(String provider) {
				Log.i("onProviderDisabled", "come in");
			}

			@Override
			public void onProviderEnabled(String provider) {
				Log.i("onProviderEnabled", "come in");
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};
		
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
		locationManager.requestLocationUpdates(provider, 3000, (float) 10.0, locationlistener);
		
		updateLocation();
		
		MapDisplayActivity.this.markCurrentLocation();
		positionMarker.setPosition(new LatLng(lat, lng));

		MapDisplayActivity.this.setCameraPosition();
	}
	
	// Update My Location
	private void updateLocation() {
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
		}
	}
	
	// Set Camera To My Location
	public void setCameraPosition() {
		// 获取视图镜头
		cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(lat, lng)) // Sets the center of the map to
				.zoom(15)   // Sets the proportion
				.bearing(0) // Sets the orientation of the camera to east
				.tilt(20)   // Sets the tilt of the camera to 30 degrees
				.build();   // Creates a CameraPosition from the builder
		map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

	// Parse Route Point
	private List<LatLng> decodePoly(String encoded) {
		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((lat / 1E5), lng / 1E5);
			poly.add(p);
		}
		return poly;
	}
	
	// Parse Turning Info
	private void parseTurningInfo(TurningInfo turningInfo) {
		int t = 0;
		int u = 0;
		int v = 0;
		int start = 0;
	    int end = 0;
		int lastFlag = -1;
		int count = 0;
		String subStrResult = "";
		
		while (lastFlag != 1) {
			if (lastFlag == 0) {
				// last turning point
				lastFlag++;
			}
			start = strResult.indexOf("<step>", start + 1);
			end = strResult.indexOf("</step>", start + 1);
			subStrResult = strResult.substring(start + 6, end);
			//Log.e("SUB_STR", subStrResult);
		    //t = start;
			t = 0;
			start = end + 1;
			t = subStrResult.indexOf("<start_location>", t + 1);
			v = subStrResult.indexOf("<lat>", t + 1);
			u = subStrResult.indexOf("</lat>", v);
			String latString = subStrResult.substring(v + 5, u);
			Log.e("latString", latString);
			t = subStrResult.indexOf("</lat>", t + 1);
			v = subStrResult.indexOf("<lng>", t + 1);
			u = subStrResult.indexOf("</lng>", v);
			String lngString = subStrResult.substring(v + 5, u);
			Log.e("lngString", lngString);
			t = subStrResult.indexOf("<maneuver>", u + 1);
			double _lat = Double.parseDouble(latString);
			double _lng = Double.parseDouble(lngString);
			turningInfo.setLatLng(new LatLng(_lat, _lng));
			if (-1 != t) {
				u = subStrResult.indexOf("</maneuver>", t + 1);
				String maneuver =  subStrResult.substring(t + 10, u);
				Log.e("turning", maneuver);
				if ((maneuver.equals("turn-left")) || (maneuver.equals("turn-right"))
				 || (maneuver.equals("turn-slight-left")) || (maneuver.equals("turn-slight-right"))
				 || (maneuver.equals("turn-sharp-left")) || (maneuver.equals("turn-sharp-right"))) {
					if (maneuver.equals("turn-left")) {
						turningInfo.setTurningDirection(TurningDirection.Turn_left);
					} else if (maneuver.equals("turn-right")) {
						turningInfo.setTurningDirection(TurningDirection.Turn_right);
					} else if (maneuver.equals("turn-slight-left")) {
						turningInfo.setTurningDirection(TurningDirection.Turn_slight_left);
					} else if (maneuver.equals("turn-slight-right")) {
		 		        turningInfo.setTurningDirection(TurningDirection.Turn_slight_right);
					} else if (maneuver.equals("turn-sharp-left")) {
						turningInfo.setTurningDirection(TurningDirection.Turn_sharp_left);
					} else if (maneuver.equals("turn-sharp-right")) {
		 		        turningInfo.setTurningDirection(TurningDirection.Turn_sharp_right);
					} else {
						turningInfo.setTurningDirection(TurningDirection.Go_Straight);
				    }
			    }
		 	} else {
		 		turningInfo.setTurningDirection(TurningDirection.Go_Straight);  
		 	}
			if (strResult.lastIndexOf("</step>") == strResult.indexOf("</step>", start + 1)) {
				lastFlag = 0;
			}
			count++;
		 	Log.i("start", String.valueOf(start));
		}
		
		turningInfo.setStepCount(count);
		
	 	Log.i("start", String.valueOf(start));
	}
	
	//
	public void drawRoute(double startLat, double startLng, double endLat, double endLng){
		//
		String url = "http://maps.google.com/maps/api/directions/xml?origin=" + startLat + "," + startLng + 
				      "&destination=" + endLat + "," + endLng + 
				      "&sensor=true&avoid=highways&mode=walking";
		
		Log.e("url", url);
		
		progressdialog = ProgressDialog.show(MapDisplayActivity.this, "Loading...", "Please Wait", true);
		
		new GetRouteTask().execute(url);
		
		while(asyncTAG == 0){
			//do nothing
		}
		
		if (-1 == strResult.indexOf("<status>OK</status>")){
			Toast.makeText(this, "Navigation Information Not Found", Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
		
		//Get Turning Info
		turningInfo = new TurningInfo();
		parseTurningInfo(turningInfo);
		Log.e("ccccccccccccount", String.valueOf(turningInfo.getStepCount()));
		for (int i = 0; i < turningInfo.getStepCount(); i++) {
			Log.e("lat", String.valueOf(turningInfo.getLatLng(i).latitude));	
			Log.e("lng", String.valueOf(turningInfo.getLatLng(i).longitude));	
			Log.e("turnD", String.valueOf(turningInfo.getTurningDirection(i)));	
		}
		
		int pos = strResult.indexOf("<overview_polyline>");
		pos = strResult.indexOf("<points>", pos + 1);
		int pos2 = strResult.indexOf("</points>", pos);
		strResult = strResult.substring(pos + 8, pos2);
		
		// Parse XML String to Points
		points = decodePoly(strResult);
		
		LatLng last = null;
		
		for (int i = 0; i < points.size() - 1; i++) {
			LatLng src = points.get(i);
			LatLng dest = points.get(i + 1);
			last = dest;
			polyline = map.addPolyline(new PolylineOptions()
					.add(new LatLng(src.latitude, src.longitude),
							new LatLng(dest.latitude, dest.longitude))
					.width(4).color(Color.BLUE));
		}
		
		if (points.size() >= 2){
			//
			//map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}

	}
	
	//
	private double calculateDistance(double lat_a, double lng_a, double lat_b, double lng_b) {

	       double radLat1 = (lat_a * Math.PI / 180.0);
	       double radLat2 = (lat_b * Math.PI / 180.0);
	       double a = radLat1 - radLat2;
	       double b = (lng_a - lng_b) * Math.PI / 180.0;
	       double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
	                    + Math.cos(radLat1) * Math.cos(radLat2)
	                    * Math.pow(Math.sin(b / 2), 2)));

	       s = s * EARTH_RADIUS;
	       s = Math.round(s * 10000) / 10000;
	       return s;
	    }
	
	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		setIconEnable(menu, true);  
		
		MenuItem item1 = menu.add(0, Menu.FIRST + 1, 0, "Select Destination");
		//item1.setIcon(R.drawable.pointmenu); 
		MenuItem item2 = menu.add(0, Menu.FIRST + 2, 0, "Navigate");
		//item2.setIcon(R.drawable.navigatemenu); 
		MenuItem item3 = menu.add(0, Menu.FIRST + 3, 0, "Reset");
		//item3.setIcon(R.drawable.resetmenu); 
		MenuItem item4 = menu.add(0, Menu.FIRST + 4, 0, "Back");
		//item4.setIcon(R.drawable.backmenu); 
		return true;
	}
	
    @Override  
    public boolean onPrepareOptionsMenu(Menu menu)   
    {  
        // TODO Auto-generated method stub  
        return super.onPrepareOptionsMenu(menu);  
    }  
      
    //enable为true时，菜单添加图标有效，enable为false时无效。4.0系统默认无效  
    private void setIconEnable(Menu menu, boolean enable)  
    {  
        try   
        {  
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");  
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);  
            m.setAccessible(true);  
              
            m.invoke(menu, enable);  
              
        } catch (Exception e)   
        {  
            e.printStackTrace();  
        }  
    }  	
	
	// Menu Event
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case Menu.FIRST + 1:
            // set a mark
			if(destinationMarker == null) {
				markDestinationLocation();				
			}
			break;
		case Menu.FIRST + 2:
            // start navigating
			if(polyline != null){
				polyline.remove();
				map.clear();
				markCurrentLocation();
				markDestinationLocation();
				MapDisplayActivity.this.setCameraPosition();
			}
			asyncTAG = 0;
			drawRoute(lat, lng, endlat, endlng);
			Toast.makeText(getBaseContext(), "Approximately "+ String.valueOf(calculateDistance(lat, lng, endlat, endlng)) +" meters", Toast.LENGTH_SHORT).show();
			navigate = true;
			break;
		case Menu.FIRST + 3:
            // cancel navigating
			navigate = false;
			polyline.remove();
			map.clear();
			destinationMarker = null;
			markCurrentLocation();
			MapDisplayActivity.this.setCameraPosition();
			break;	
		case Menu.FIRST + 4:
			MapDisplayActivity.this.finish();
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event) {
         // return back
 		 if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
 			// Go Back
 			MapDisplayActivity.this.finish();
 			return true;
 		 }
 		 return false;
 	}
	
	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), "Dragging stop", Toast.LENGTH_SHORT).show();
		endlat = marker.getPosition().latitude;
		endlng = marker.getPosition().longitude;
		//Log.e("!!!!!!!!!!!!!!!", String.valueOf(endlat));
		//Log.e("!!!!!!!!!!!!!!!", String.valueOf(endlng));
	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		Toast.makeText(getBaseContext(), "Dragging start", Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private class GetRouteTask extends AsyncTask<String, Void, String>{
		//
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
		}

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			return getRoute(url[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			progressdialog.dismiss();
		}
		
		//Request route from the Google Direction API 
	    private String getRoute(String url){
			HttpGet get = new HttpGet(url);
			try {
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpClient httpClient = new DefaultHttpClient(httpParameters); 
				
				HttpResponse httpResponse = null;
				httpResponse = httpClient.execute(get);
				if (httpResponse.getStatusLine().getStatusCode() == 200){
					strResult = EntityUtils.toString(httpResponse.getEntity());
				}
			} catch (Exception e) {
				return "error";
			}
			asyncTAG = 1;
			return strResult;
	    }
	}
	
	private class TurningInfo{
		//
		private List<LatLng> latlng;		
		private List<TurningDirection> turningDirection;
		private int stepCount;
		
		public TurningInfo() {
			latlng = new ArrayList<LatLng> ();
			turningDirection = new ArrayList<TurningDirection> ();
			stepCount = 0;
		}
		
		public void setLatLng(LatLng tmp) {
			latlng.add(tmp);
		}
		
		public void setTurningDirection(TurningDirection tmp) {
			turningDirection.add(tmp);
		}
		
		public void setStepCount(int i) {
			stepCount = i;
		}
		
		public LatLng getLatLng(int index) {
			return latlng.get(index);
		}
		
		public TurningDirection getTurningDirection(int index) {
			return turningDirection.get(index);
		}
		
		public int getStepCount() {
			return stepCount;
		}
	}

}
