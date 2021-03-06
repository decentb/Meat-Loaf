package com.cs301w01.meatload.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import com.cs301w01.meatload.R;
import com.cs301w01.meatload.adapters.SimpleTagAdapter;
import com.cs301w01.meatload.adapters.SpinnerAlbumAdapter;
import com.cs301w01.meatload.controllers.AlbumManager;
import com.cs301w01.meatload.controllers.MainManager;
import com.cs301w01.meatload.controllers.PictureManager;
import com.cs301w01.meatload.model.Album;
import com.cs301w01.meatload.model.Picture;
import com.cs301w01.meatload.model.Tag;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Takes a picture and displays it in exploded view along with important
 * metadata including tags, date, etc.
 * <p>
 * Gives the user an exploded view of the picture being edited.
 * <p>
 * Allows user to change certain metadata such as tags and album.
 * <p>
 * TODO: Carriage Return (Enter key) needs to be handled by Picture Name EditText.
 * 
 * @author Blake Bouchard
 */
public class EditPictureActivity extends Skindactivity {

	private MainManager mainManager;
	private AlbumManager albumManager;
	private PictureManager pictureManager;
	private ListView tagListView;
	private EditText pictureNameEditText;
	private ImageView pictureView;
	private Spinner albumSpinner;
	private AutoCompleteTextView addTagEditText;

	@Override
	public void update(Object model) {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.edit_picture);

		// Set up MainManager
		mainManager = new MainManager(this);
		albumManager = new AlbumManager(this);

		pictureView = (ImageView) findViewById(R.id.pictureView);

		// Get picture object from Intent's extras bundle
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Picture picture = (Picture) extras.getSerializable("picture");

		// Set up a new PictureManager using the Picture object passed via the
		// intent
		pictureManager = new PictureManager(this, picture);

		populateTextFields();

		createListeners();

	}

	@Override
	protected void onResume() {
		super.onResume();
		pictureManager.setContext(this);
	}

	protected void populateTags() {

		// Add Tag field logic
		addTagEditText = (AutoCompleteTextView) findViewById(R.id.addTagEditText);

		ArrayList<Tag> allTags = mainManager.getAllTags();
		ArrayList<String> tagStrings = new ArrayList<String>();
		for (Tag tag : allTags) {
			tagStrings.add(tag.getName());
		}
		ArrayAdapter<String> stringAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, tagStrings);
		addTagEditText.setAdapter(stringAdapter);

		// Tag List View
		tagListView = (ListView) findViewById(R.id.tagList);
		ArrayList<Tag> pictureTags = pictureManager.getTags();
		SimpleTagAdapter tagAdapter = new SimpleTagAdapter(this, R.layout.simple_list_item,
				pictureTags);
		tagListView.setAdapter(tagAdapter);

	}

	/**
	 * Fills the text and image fields on the screen with a current picture.
	 * 
	 * @see <a
	 *      href="http://codehenge.net/blog/2011/05/customizing-android-listview-item-layout/">
	 *      http://codehenge.net/blog/2011/05/customizing-android-listview-item-layout/</a>
	 */
	protected void populateTextFields() {

		// Picture ImageView
		Picture picture = pictureManager.getPicture();
		pictureView = (ImageView) findViewById(R.id.pictureView);
		pictureView.setImageDrawable(Drawable.createFromPath(picture.getPath()));

		// Picture Name EditText
		pictureNameEditText = (EditText) findViewById(R.id.pictureNameEditText);
		pictureNameEditText.setText(picture.getName());

		// Set dateView to String representation of Date in Picture object
		TextView dateView = (TextView) findViewById(R.id.dateView);
		dateView.setText(picture.getDate().toString());

		// AlbumView Spinner
		albumSpinner = (Spinner) findViewById(R.id.albumSpinner);
		ArrayList<Album> allAlbums = albumManager.getAllAlbums();
		SpinnerAlbumAdapter spinnerAdapter = new SpinnerAlbumAdapter(this, R.layout.spinner_item,
				allAlbums);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		albumSpinner.setAdapter(spinnerAdapter);

		// Find the album object in allAlbums associated with the name of the
		// album in the picture
		for (Album album : allAlbums) {
			if (picture.getAlbumName().equals(album.getName())) {
				albumSpinner.setSelection(allAlbums.indexOf(album));
				break;
			}
		}

		populateTags();
	}

	protected void createListeners() {

		// Send Email Button logic
		Button sendEmailButton = (Button) findViewById(R.id.sendEmailButton);
		sendEmailButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				openSendEmailActivity();
			}

		});

		// Catch enter key on AddTag field
		addTagEditText.setOnKeyListener(new View.OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					addTag();
					return true;
				}
				return false;
			}
		});

		// Add Tag button logic
		Button addTagButton = (Button) findViewById(R.id.addTagButton);
		addTagButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				addTag();
			}

		});

		// Tag List Long Click Listener logic
		tagListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				deleteTagDialog((Tag) parent.getItemAtPosition(position));
				return true;
			}

		});

		// Delete Button logic
		Button deletePicButton = (Button) findViewById(R.id.deletePictureButton);
		deletePicButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				deletePictureDialog();
			}

		});

		// Save Picture logic
		Button savePictureButton = (Button) findViewById(R.id.savePictureButton);
		savePictureButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				savePicture();
			}

		});
	}

	/**
	 * Takes the text from the addTagEditText field and adds it to the staged
	 * "Add Tags" in pictureManager.
	 * 
	 * @see PictureManager
	 */
	private void addTag() {

		String tag = addTagEditText.getText().toString().trim();
		if (tag.length() == 0) {
			// Return if empty/whitespace tag
			return;
		} else if (tag.length() > mainManager.getMaxTagName()) {
			// if tag is too long cut it to max tag length
			tag = tag.substring(0, mainManager.getMaxTagName());
		}

		pictureManager.addTag(tag);

		populateTags();
		addTagEditText.setText("");
	}

	/**
	 * This method implements the logic that occurs when the "savePicture"
	 * button is clicked. Checks for any changes in the pictures meta-data,
	 * updates those, and then finished the activity.
	 */
	private void savePicture() {

		Picture picture = new Picture(pictureNameEditText.getText().toString(),
				((Album) albumSpinner.getSelectedItem()).getName());
		pictureManager.savePicture(picture);
		finish();
	}
	
	private void deletePictureDialog() {

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Delete Picture?");
		alert.setMessage("Are you sure?");

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				pictureManager.deletePicture();
				finish();
			}

		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}

		});

		alert.show();

	}

	private void deleteTagDialog(final Tag tag) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Delete Tag?");
		alert.setMessage("Are you sure?");

		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				pictureManager.deleteTag(tag.getName());
				populateTags();
				dialog.dismiss();
			}

		});

		alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int whichButton) {
				dialog.dismiss();
			}

		});

		alert.show();
	}

	private void openSendEmailActivity() {
		Intent sendEmail = new Intent();
		sendEmail.setClassName("com.cs301w01.meatload",
				"com.cs301w01.meatload.activities.SendEmailActivity");
		sendEmail.putExtra("picture", pictureManager.getPicture());

		startActivity(sendEmail);
	}
	
}