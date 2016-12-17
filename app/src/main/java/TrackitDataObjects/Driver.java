package TrackitDataObjects;

/**
 *The Driver Model is designed to hold all the relevant information
 * for a driver within the AVTA database.
 */

public class Driver
{

    private String email;
    private String password;
    private String FirstName;
    private String LastName;
    private String phoneNumber;


    /******************************************************
      NO Argument constructor
     */
    public Driver()
    {

    }//driver() Ends


    /*******************************************************
     Constructor with parameters
     */
    public Driver (String firstName, String lastName, String phoneNumber, String email)
    {
    }//Driver(String, String) Ends


    /*******************************************************
     * Class Getter and Setter methods
     * @return
     */


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /************END OF GETTERS AND SETTERS***************/


}//Drivere Class Ends