package io.keepcoding.pickandgol.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.keepcoding.pickandgol.R;
import io.keepcoding.pickandgol.model.Event;


public class NewEventFragment extends Fragment {
    public interface Listener {
        void onSaveButtonPushed(final Event newEvent);
    }

    @BindView(R.id.new_event_name_edit_text)
    EditText nameText;

    @BindView(R.id.new_event_date_text)
    EditText dateText;

    @BindView(R.id.new_event_time_text)
    TextView timeText;

    @BindView(R.id.new_event_category_spinner)
    Spinner categorySpinner;

    @BindView(R.id.new_event_save_button)
    Button saveButton;

    private Listener listener;
    private Calendar dateSelected;

    public NewEventFragment() {
        dateSelected = Calendar.getInstance();
    }

    public static NewEventFragment getInstance() {
        return new NewEventFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);
        ButterKnife.bind(this, view);

        setupSaveButton();
        setupDateText();
        setupTimeText();

        return view;
    }

    private void setupTimeText() {
        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTimeField();
            }
        });
    }

    private void setupDateText() {
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateField();
            }
        });
    }

    private void setDateField() {
        Calendar newCalendar = dateSelected;
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dateSelected.set(year, month, day);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                dateText.setText(df.format(dateSelected.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setTimeField() {
        Calendar newCalendar = dateSelected;
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                dateSelected.set(Calendar.HOUR_OF_DAY, hour);
                dateSelected.set(Calendar.MINUTE, minute);
                SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
                timeText.setText(df.format(dateSelected.getTime()));
            }
        }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onSaveButtonPushed(getNewEvent());
                }
            }
        });
    }

    private Event getNewEvent() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z'('Z')'", Locale.US);
        String date = dateFormat.format(dateSelected.getTime());


        List<String> pubs = new ArrayList<>();
        pubs.add("589e3150a28ea6ad0bc1ca13");
        Event event = new Event()
                .setName(nameText.getText().toString())
                .setDate(date)
                // XXX .setDate("2017-03-20T20:30:00.000Z")
                .setPubs(pubs);

        return event;
    }
}
