package com.cs301w01.meatload.authentication.querygenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cs301w01.meatload.authentication.model.Password;
import com.cs301w01.meatload.authentication.model.Patient;
import com.cs301w01.meatload.authentication.model.Specialist;
import com.cs301w01.meatload.authentication.model.User;
import com.cs301w01.meatload.model.Album;
import com.cs301w01.meatload.model.querygenerators.AlbumQueryGenerator;
import com.cs301w01.meatload.model.querygenerators.QueryGenerator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Handles all queries relating to the user systems including subscription, login, sign up, and
 * any other functionality related to users.
 * 
 * @author Derek Dowling
 *
 */
public class UserQueryGenerator extends QueryGenerator {

    //static strings used by this class
    public static final String TABLE_NAME_USERS = "users";
    public static final String TABLE_NAME_USER_ALBUMS = "useralbums";
    public static final String SPECIALIST_ROLE = "Specialist";
    public static final String PATIENT_ROLE = "Patient";
    
    //col's specific to this class
    public static final String COL_ROLE = "role";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_SALT = "salt";
    public static final String COL_EMAIL = "email";
    public static final String COL_SPECIALIST_ID = "s_id";
    public static final String COL_PATIENT_ID = "p_id";
    public static final String COL_USER_ID = "u_id";
    public static final String COL_ALBUM_ID = "a_id";
    
    //sql for creating the user info table
    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_NAME_USERS +" (" +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_NAME + " TEXT, " +
            COL_USERNAME + " TEXT, " +
            COL_EMAIL + " TEXT, " +
            COL_PASSWORD + " TEXT, " +
            COL_SALT + " TEXT, " +
            COL_ROLE + " TEXT );";
    
    //sql for creating the album ownership table
    public static final String CREATE_USER_ALBUMS_TABLE =
            "CREATE TABLE " + TABLE_NAME_USER_ALBUMS + " " +
            COL_ID + " INTEGER PRIMARY KEY, " +
            COL_USER_ID + " INTEGER " +
                    "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " +
                    TABLE_NAME_USERS + "( " + COL_ID + "), " +
            COL_ALBUM_ID + " INTEGER " +
                    "FOREIGN KEY(" + COL_ALBUM_ID + ") REFERENCES " +
                    AlbumQueryGenerator.TABLE_NAME + "( " + COL_ID + "));";
            
    
    
    public UserQueryGenerator(Context context) {
        super(context);
    }

    /**
     * Inserts a new user into the database and returns their primary key id.
     * @param name
     * @param email
     * @param username
     * @param password
     * @param role
     * @return long id
     */
    public long insertNewUser(String name, String email, String username, Password password, String role) {

        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_EMAIL, email);
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password.getPw());
        cv.put(COL_SALT, password.getSalt());
        cv.put(COL_ROLE, role);

        return db.insert(TABLE_NAME_USERS, COL_ID, cv);

    }

    /**
     * Gets all albums associated with a specific userID.
     * @param userID
     * @return Collection<Album>
     */
    public Collection<Album> getAlbumsByUserID(int userID) {

        String query = "SELECT " + COL_ALBUM_ID + " " +
                       "FROM " + TABLE_NAME_USER_ALBUMS + " " +
                       "WHERE " + COL_USER_ID + " = '" + userID + "'";

        Cursor ids = db.performRawQuery(query);

        Collection<Integer> albumIDs = new ArrayList<Integer>();
        Collection<Album> albums = new ArrayList<Album>();

        if(ids.getCount() == 0)
            return albums;

        while(!ids.isAfterLast()) {

            albumIDs.add(ids.getInt(ids.getColumnIndex(COL_ALBUM_ID)));

            ids.moveToNext();

        }

        AlbumQueryGenerator aQ = new AlbumQueryGenerator(context);

        for(Integer id : albumIDs) {

            albums.add(aQ.getAlbumByID(id));

        }

        return albums;
    }

    

    /**
     * Returns a user of type patient.
     * @param patientID int
     * @return Patient object
     */
    public Patient getPatientByID(int patientID) {

        //need: String name, String email, Collection<Album> albums
        String query = "SELECT " + COL_NAME + ", " + COL_EMAIL + " " +
                       "FROM " + TABLE_NAME_USERS + " " +
                       "WHERE " + COL_ID + " = '" + patientID + "'";

        Cursor c = db.performRawQuery(query);

        String name = c.getString(c.getColumnIndex(UserQueryGenerator.COL_NAME));
        String email = c.getString(c.getColumnIndex(UserQueryGenerator.COL_EMAIL));

        Collection<Album> albums = getAlbumsByUserID(patientID);

        return new Patient(name, email, albums, 0);

    }

    public void addNewUserAlbum(Album a) {

    }

    public void deleteUserAlbum(int albumID) {

    }

    public void updateUserInfo(User u) {

    }

    public String getUserSalt(String username){

        String query = "SELECT " + COL_SALT + " " +
                       "FROM " + TABLE_NAME_USERS + " " +
                       "WHERE " + COL_USERNAME + " = '" + username + "'";

        Cursor c = db.performRawQuery(query);

        if(c.getCount() == 0) {

            return null;

        } else {

            return c.getString(c.getColumnIndex(COL_SALT));

        }

    }

    /**
     * Attempts to log a user in, if the credentials are correct, will return one result with the users basic
     * information which can be used to retrieve other data. Otherwise returns a cursor with count == 0.
     * @param userName
     * @param password
     * @return Cursor
     */
    public Cursor logUserIn(String userName, String password) {

        
        String query = "SELECT " +
                        COL_ID + ", " + COL_NAME + ", " + COL_USERNAME + ", " + COL_EMAIL + ", " + COL_ROLE + " " +
                       "FROM " + TABLE_NAME_USERS + " " +
                       "WHERE " + COL_USERNAME + " = '" + userName + "' " +
                       "AND " + COL_PASSWORD + " = '" + password + "'";
        
        return db.performRawQuery(query);
        
    }

    public User getSpecialistByID(int id, Collection<Patient> patients) {

        String query = "SELECT " + COL_NAME + ", " + COL_EMAIL + " " +
                "FROM " + TABLE_NAME_USERS + " " +
                "WHERE " + COL_ID + " = '" + id + "'";

        Cursor c = db.performRawQuery(query);

        String name = c.getString(c.getColumnIndex(UserQueryGenerator.COL_NAME));
        String email = c.getString(c.getColumnIndex(UserQueryGenerator.COL_EMAIL));

        return new Specialist(name, email, patients);

    }
}
