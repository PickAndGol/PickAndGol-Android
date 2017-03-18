package io.keepcoding.pickandgol.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.adapter.IntegerStringSpinnerAdapter;
import io.keepcoding.pickandgol.fragment.DatePickerFragment;
import io.keepcoding.pickandgol.fragment.TimePickerFragment;
import io.keepcoding.pickandgol.interactor.CreateEventInteractor;
import io.keepcoding.pickandgol.interactor.CreateEventInteractor.CreateEventInteractorListener;
import io.keepcoding.pickandgol.interactor.GetCategoriesInteractor;
import io.keepcoding.pickandgol.manager.image.ImageManager;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImagePickingListener;
import io.keepcoding.pickandgol.manager.image.ImageManager.ImageProcessingListener;
import io.keepcoding.pickandgol.manager.session.SessionManager;
import io.keepcoding.pickandgol.model.CategoryAggregate;
import io.keepcoding.pickandgol.model.Event;
import io.keepcoding.pickandgol.model.Pub;
import io.keepcoding.pickandgol.navigator.Navigator;
import io.keepcoding.pickandgol.util.ErrorSuccessListener;
import io.keepcoding.pickandgol.util.PermissionChecker;
import io.keepcoding.pickandgol.util.Utils;

import static android.view.View.VISIBLE;
import static io.keepcoding.pickandgol.manager.image.ImageManagerSettings.IMAGE_PICKER_REQUEST_CODE;
import static io.keepcoding.pickandgol.util.PermissionChecker.PermissionTag.CAMERA_SET;


/**
 * This class represents the activity with the New Event form.
 */
public class NewEventActivity extends AppCompatActivity {

    private final static String LOG_TAG = "NewEventActivity";

    public final static String PUB_MODEL_KEY = "PUB_MODEL_KEY";
    public final static String NEW_EVENT_KEY = "NEW_EVENT_KEY";


    private Pub model;

    private SessionManager sm;
    private ImageManager im;

    private PermissionChecker cameraChecker;

    Integer yyyy, mm, dd, hh, mins;
    private File imageFileToUpload;
    private CategoryAggregate categories;

    @BindView(R.id.activity_new_event_name_text)         EditText txtName;
    @BindView(R.id.activity_new_event_category_spinner) Spinner spnCategory;
    @BindView(R.id.activity_new_event_date_text)         EditText txtDate;
    @BindView(R.id.activity_new_event_time_text)         TextView txtTime;
    @BindView(R.id.activity_new_event_description_text) EditText txtDescription;
    @BindView(R.id.activity_new_event_image_holder)      ImageView imageHolder;
    @BindView(R.id.activity_new_event_image_button)     Button btnRemoveImage;
    @BindView(R.id.activity_new_event_button_cancel)    Button btnCancel;
    @BindView(R.id.activity_new_event_button_create)    Button btnCreate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        ButterKnife.bind(this);

        cameraChecker = new PermissionChecker(CAMERA_SET, this);

        sm = SessionManager.getInstance(this);
        im = ImageManager.getInstance(this);

        setupActionBar();
        setupCategorySpinner();
        setupPickers();
        setupButtons();

        loadModel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.new_event_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finishActivity(null, null);
                return true;

            case R.id.new_event_menu_reset:
                resetForm();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Reset all data in the form
    private void resetForm() {

        txtName.setText("");
        txtDate.setText("");
        txtTime.setText("");
        txtDescription.setText("");

        spnCategory.setSelection(0);

        yyyy = mm = dd = hh = mins = null;

        imageFileToUpload = null;
        btnRemoveImage.setVisibility(View.INVISIBLE);
        im.loadImage(R.drawable.add_image_placeholder, imageHolder);

        txtName.requestFocus();
    }

    // Set the layout toolbar as the activity action bar
    // and show the home button
    private void setupActionBar() {

        setTitle(getString(R.string.new_event_activity_create_event));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }


    private void setupCategorySpinner() {
        GetCategoriesInteractor interactor = new GetCategoriesInteractor();
        interactor.execute(this, new GetCategoriesInteractor.Listener() {
            @Override
            public void onFail(String message) {
                Log.e(LOG_TAG, message);
            }

            @Override
            public void onSuccess(CategoryAggregate categories) {
                NewEventActivity.this.categories = categories;
                IntegerStringSpinnerAdapter adapter = IntegerStringSpinnerAdapter.createAdapterForCategoriesSpinner(NewEventActivity.this, categories, getString(R.string.new_event_activity_spinner_default_text));
                spnCategory.setAdapter(adapter);
            }
        });
    }

    private void setupButtons() {

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity(null, null);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Event validatedData = validateFormData();

                if ( validatedData != null )
                    uploadImageIfNecessaryThenRegisterNewEvent(validatedData);
            }
        });

        btnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                imageFileToUpload = null;
                btnRemoveImage.setVisibility(View.INVISIBLE);
                im.loadImage(R.drawable.add_image_placeholder, imageHolder);
            }
        });
    }


    private void setupPickers() {

        // Date & Time pickers:
        yyyy = mm = dd = hh = mins = null;

        txtDate.setInputType(InputType.TYPE_NULL);
        txtTime.setInputType(InputType.TYPE_NULL);

        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerFragment().setDatePickerListener(new DatePickerFragment.DatePickerListener() {
                    @Override
                    public void onDateSet(int year, int month, int day) {
                        yyyy = year;
                        mm = month; // BEWARE: month range is 0 - 11 (i.e. January is 0)
                        dd = day;

                        String strYear = ""+ year;
                        String strMonth = (month < 10) ? "0"+ (mm+1) : ""+ (mm+1);
                        String strDay = (day < 10) ? "0"+ day : ""+ day;

                        txtDate.setText(strDay +"/"+ strMonth +"/"+ strYear);
                    }
                })
                .show(getSupportFragmentManager(), "datePicker");
            }
        });

        txtTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerFragment().setTimePickerListener(new TimePickerFragment.TimePickerListener() {
                    @Override
                    public void onTimeSet(int hourOfDay, int minute) {
                        hh = hourOfDay;
                        mins = minute;

                        String strHour = (hourOfDay < 10) ? "0"+ hourOfDay : ""+ hourOfDay;
                        String strMin = (minute < 10) ? "0"+ minute : ""+ minute;

                        txtTime.setText(strHour +":"+ strMin);
                    }
                })
                .show(getSupportFragmentManager(), "timePicker");
            }
        });


        // Image picker:
        imageFileToUpload = null;

        imageHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // If no image chosen, show the image picker
                if (imageFileToUpload == null)
                    showImagePicker();

                // If an image is already chosen
                else {
                    // TODO: show selected image in full screen
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If we are coming from the image picker
        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {

            im.handleImagePickerResult(NewEventActivity.this, requestCode, resultCode, data,
                                       new ImagePickingListener() {

                @Override
                public void onImagePicked(String imagePath) {

                    if (imagePath == null) {
                        Log.e(LOG_TAG, "Failed to get the path from the image picker");
                        return;
                    }

                    else {
                        processImageThenLoadIt( new File(imagePath) );
                    }

                }
            });
        }
    }


    private void showImagePicker() {

        // Check if we have permission to access the camera, before opening the image picker
        cameraChecker.checkBeforeAsking(new PermissionChecker.CheckPermissionListener() {
            @Override
            public void onPermissionDenied() {
                String title = "Camera access denied";
                String msg = "Pick And Gol will not be able to take images from your device camera.";
                Utils.simpleDialog(NewEventActivity.this, title, msg);
            }

            @Override
            public void onPermissionGranted() {
                im.showImagePicker(NewEventActivity.this);
            }
        });
    }


    private void processImageThenLoadIt(File sourceImageFile) {

        if (sourceImageFile == null || ! sourceImageFile.isFile() )
            return;

        im.processImage(sourceImageFile, new ImageProcessingListener() {

            @Override
            public void onProcessError(Exception error) {
                Utils.simpleDialog(NewEventActivity.this, "Unable to process image", error.toString());

                imageFileToUpload = null;
                btnRemoveImage.setVisibility(View.INVISIBLE);
                im.loadImage(R.drawable.add_image_placeholder, imageHolder);
            }

            @Override
            public void onProcessSuccess(File resizedFile) {

                imageFileToUpload = resizedFile;
                btnRemoveImage.setVisibility(VISIBLE);
                im.loadImage(imageFileToUpload, imageHolder, R.drawable.error_placeholder);
            }
        });
    }


    private void loadModel() {

        Intent i = getIntent();
        model = (Pub) i.getSerializableExtra(PUB_MODEL_KEY);

        if (model == null)
            finishActivity(null, new Exception("No pub has been provided to register the new event"));
    }


    private Event validateFormData() {

        Event tempEvent = null;     // This object is just a container of some data from the form

        String eventName = txtName.getText().toString();
        Integer selectedCategoryIndex = ((int) spnCategory.getSelectedItemId()) - 1;
        String eventDescription = txtDescription.getText().toString();

        if (eventName.equals("")) {
            Utils.simpleDialog(this, "Invalid data", "You must specify a name for the event.");
            txtName.requestFocus();
        }

        else if (selectedCategoryIndex < 0) {
            Utils.simpleDialog(this, "Invalid data", "You must select a category for the event.");
            spnCategory.requestFocus();
        }

        else if (yyyy == null || mm == null || dd == null || hh == null || mins == null) {
            Utils.simpleDialog(this, "Invalid data", "You must specify date & hour for the event.");
            txtDate.requestFocus();
        }

        // All data are correct, prepare the temp Event
        else {
            Date eventDate = Utils.getDateFromIntegers(yyyy, mm, dd, hh, mins);
            String eventCategory = null;
            if (categories.get(selectedCategoryIndex) != null) {
                eventCategory = categories.get(selectedCategoryIndex).getId();
            }

            if (eventDescription.equals(""))
                eventDescription = null;

            String remoteFileName = null;
            if (imageFileToUpload != null)
                remoteFileName = UUID.randomUUID().toString() +".jpg";  // note: this is NOT the full URL

            List<String> eventPubs = new ArrayList<>();
            eventPubs.add(model.getId());


            tempEvent = new Event("", eventName, eventDate, eventDescription, remoteFileName, eventCategory, eventPubs);
        }

        return tempEvent;
    }


    private void uploadImageIfNecessaryThenRegisterNewEvent(final Event event) {

        if (event.getPhotoUrl() == null) {
            registerNewEvent(event);
            return;
        }

        String sourcePath = imageFileToUpload.getAbsolutePath();
        String remoteFileName = event.getPhotoUrl();

        // First, attempt to upload to the cloud the selected image
        doUploadImage(sourcePath, remoteFileName, new ErrorSuccessListener() {
            @Override
            public void onError(@Nullable Object result) {
                Utils.simpleDialog(NewEventActivity.this, "Image Upload error",
                        "The selected image could not be uploaded. Please try again.");
            }

            @Override
            public void onSuccess(@Nullable Object result) {
                registerNewEvent(event);
            }
        });
    }


    private void registerNewEvent(Event event) {

        String name = event.getName();
        Date date = event.getDate();
        String pubId = event.getPubs().get(0);
        String categoryId = event.getCategory();
        String description = event.getDescription();
        String photoUrl = im.getRemoteImageUrl( event.getPhotoUrl() );
        String token = sm.getSessionToken();

        final ProgressDialog pDialog = Utils.newProgressDialog(this, "Registering event...");
        pDialog.show();

        new CreateEventInteractor().execute(this, name, date, pubId, categoryId,
                                            description, photoUrl, token,
                                            new CreateEventInteractorListener() {

            @Override
            public void onCreateEventFail(Exception e) {
                pDialog.dismiss();

                Utils.simpleDialog(NewEventActivity.this, "Error registering new event", e.getMessage());
            }

            @Override
            public void onCreateEventSuccess(final Event createdEvent) {
                pDialog.dismiss();

                Utils.simpleDialog(NewEventActivity.this,
                                   "New Event",
                                   "The event '"+ createdEvent.getName() +"' has been created.",
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialogInterface, int i) {
                                           finishActivity(createdEvent, null);
                                       }
                });
            }
        });
    }


    // Upload file to Amazon S3 bucket
    private void doUploadImage(final String filePath, final String remoteFileName, final ErrorSuccessListener listener) {

        if( ! new File(filePath).isFile() ) {
            Log.e(LOG_TAG, "An error occurred: '"+ filePath +"' does not exist or is not a file");
            Utils.simpleDialog(NewEventActivity.this, "Image Upload error",
                    "The selected image could not be uploaded. Please try again.");
            return;
        }

        File sourceFile = new File(filePath);

        int kbTotal = (int) (sourceFile.length() / 1024.0f);         // progress will be measured in KB
        String fileSize = Utils.readableSize(sourceFile.length());   // a string ended with B/KB/MB/...

        Log.d(LOG_TAG, "Uploading file '"+ filePath +"' ("+ fileSize +")...");
        final ProgressDialog pDialog =
                Utils.newProgressBarDialog(this, kbTotal, "Uploading image ("+ fileSize +")...");
        pDialog.show();


        im.uploadImage(sourceFile, remoteFileName, new ImageManager.ImageUploadListener() {

            @Override
            public void onProgressChanged(int transferId, long bytesCurrent, long bytesTotal) {
                int kbCurrent = (int) (bytesCurrent / 1024.0f);
                pDialog.setProgress( kbCurrent );
            }

            @Override
            public void onImageUploadError(int transferId, Exception e) {
                pDialog.dismiss();
                Log.e(LOG_TAG, "[id "+ transferId +"] An error occurred: "+ e.toString());

                listener.onError(null);
            }

            @Override
            public void onImageUploadCompletion(int transferId) {
                pDialog.dismiss();
                Log.d(LOG_TAG, "[id "+ transferId +"] File '" + filePath + "' has been stored as: "+ remoteFileName);

                listener.onSuccess(null);
            }
        });
    }


    private void finishActivity(@Nullable Event newEvent, @Nullable Exception error) {

        if (error != null)
            Log.e(LOG_TAG, "Error: "+ error.toString());

        Navigator.backFromNewEventActivity(this, newEvent);
    }





}
