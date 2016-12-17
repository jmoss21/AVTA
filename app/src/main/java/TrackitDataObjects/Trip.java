package TrackitDataObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * The Trip Class is designed to hold all the information for a trip and be the object that
 * the MapActivity interface updates in order to keep track of a Driver initiated trip.
 * Additionally, it can be used to contain the data about a previously recorded trip.

 */

public class Trip
{

    /*
    Trip's attributes
     */
    private String startDateAndTime;
    private double milesTravelled;
    private String totalTime;
    private String startPoint; //LatLng starting point of the trip
    private String endPoint; //LatLng Ending point of the trip
    private String endDateAndTime;
    private String driverID;
    private String carVin;


    /***********************************************
     *Class GETTERS and SETTERS
     */
    public Trip()
    {

    }


    public Trip(String driveerID, String carVin, String startDateAndTime) {

        this.driverID = driveerID;
        this.carVin = carVin;
        this.startDateAndTime = startDateAndTime;
    }//trip Ends


    /*************************Getters and Setters******************/

    public double getMilesTravelled() {
        return milesTravelled;
    }

    public void setMilesTravelled(double milesTravelled) {
        this.milesTravelled = milesTravelled;
    }



    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }


    public String getStartDateAndTime() {
        return startDateAndTime;
    }

    public void setStartDateAndTime() {
        this.startDateAndTime = startDateAndTime;
    }



    public String getDriverID() {
        return driverID;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getCarVin() {
        return carVin;
    }

    public void setCarVin(String carVin) {
       this.carVin = carVin;
    }

    /**********END of GEETERS AND SETTERS*******************     */


    @Override
    public String toString() {
        return  " Date: [" +startDateAndTime+ "] "+
                "\n\n Car: "+carVin +
                "\n\n Miles Travelled: [" +milesTravelled+ "miles]"+
                "\n\n StartPoint: "+startPoint +
                "\n\n EndPoint: "+endPoint;
    }//toString
}
