package com.example.mco2_itismob_gym_app_ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int START = 0;
    private static final int SELECTION = 1;
    private static final int LOGGING = 2;
    private static final int SUMMARY = 3;

    // Each position here matches the same exercise in the other arrays.
    private final int[] checkboxIds =
    {
            R.id.exerciseBenchPress, R.id.exercisePushUp,
            R.id.exerciseLatPulldown, R.id.exerciseSeatedRow,
            R.id.exerciseSquat, R.id.exerciseLegPress,
            R.id.exerciseBicepCurl, R.id.exerciseTricepPushdown
    };

    private final int[] rowIds =
    {
            R.id.logBenchPress, R.id.logPushUp,
            R.id.logLatPulldown, R.id.logSeatedRow,
            R.id.logSquat, R.id.logLegPress,
            R.id.logBicepCurl, R.id.logTricepPushdown
    };

    private final int[] exerciseNameIds =
    {
            R.string.bench_press, R.string.push_up,
            R.string.lat_pulldown, R.string.seated_row,
            R.string.squat, R.string.leg_press,
            R.string.bicep_curl, R.string.tricep_pushdown
    };

    // Remember selected exercises and typed values while the workout is open.
    private final boolean[] selected = new boolean[checkboxIds.length];
    private final String[][] entries = new String[checkboxIds.length][3];
    // Finished workouts for the History tab. Same position = same workout.
    private final ArrayList<String> historyDates = new ArrayList<>();
    private final ArrayList<String> historyDetails = new ArrayList<>();

    private int currentScreen = START;
    private OnBackPressedCallback backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setSelectedItemId(R.id.nav_workout);
        navigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_workout)
            {
                showWorkoutTab();
                return true;
            }

            if (item.getItemId() == R.id.nav_history)
            {
                // Keep what the user typed if they leave in the middle of logging
                if (currentScreen == LOGGING)
                {
                    saveEntries();
                }

                showHistory();
                return true;
            }

            return false;
        });

        // Take the user back to the previous workout step.
        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed()
            {
                if (currentScreen == LOGGING)
                {
                    saveEntries();
                    showSelection();
                }

                else
                {
                    showStart();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, backCallback);
        showStart();
    }

    // Go back to the workout step the user was on before opening History.
    private void showWorkoutTab()
    {
        if (currentScreen == SELECTION)
        {
            showSelection();
        }

        else if (currentScreen == LOGGING)
        {
            showLogging();
        }

        else if (currentScreen == SUMMARY)
        {
            // Already saved in History, so start a new workout
            resetWorkout();
        }

        else
        {
            showStart();
        }
    }

    private void showHistory()
    {
        // Back button shouldn't move through the workout steps while on History
        backCallback.setEnabled(false);
        showScreen(R.layout.screen_history);

        LinearLayout list = findViewById(R.id.historyList);
        findViewById(R.id.historyEmptyText).setVisibility(historyDates.isEmpty() ? View.VISIBLE : View.GONE);

        // Newest workout goes on top
        for (int i = historyDates.size() - 1; i >= 0; i--)
        {
            View card = getLayoutInflater().inflate(R.layout.item_history_workout, list, false);
            TextView details = card.findViewById(R.id.historyDetails);
            String text = historyDetails.get(i);

            ((TextView) card.findViewById(R.id.historyDate)).setText(historyDates.get(i));
            ((TextView) card.findViewById(R.id.historyCount)).setText(
                    getString(R.string.history_exercise_count, text.split("\n").length));
            details.setText(text);

            // Tap a workout to show or hide its exercises, sets, reps and weight
            card.setOnClickListener(view -> details.setVisibility(
                    details.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

            list.addView(card);
        }
    }

    private void showStart()
    {
        currentScreen = START;
        backCallback.setEnabled(false);
        showScreen(R.layout.screen_workout_start);
        findViewById(R.id.startWorkoutButton).setOnClickListener(view -> showSelection());
    }

    private void showSelection()
    {
        currentScreen = SELECTION;
        backCallback.setEnabled(true);
        showScreen(R.layout.screen_exercise_selection);

        // Keep exercises checked if the user comes back to this screen.
        for (int i = 0; i < checkboxIds.length; i++)
        {
            CheckBox checkbox = findViewById(checkboxIds[i]);
            checkbox.setChecked(selected[i]);
            int exerciseIndex = i;
            checkbox.setOnCheckedChangeListener((button, checked) -> selected[exerciseIndex] = checked);
        }

        findViewById(R.id.selectionBackButton).setOnClickListener(view -> showStart());
        findViewById(R.id.selectionContinueButton).setOnClickListener(view -> {
            boolean hasSelection = false;
            for (boolean isSelected : selected)
            {
                hasSelection |= isSelected;
            }

            // Error catching thing - Ask the user to pick an exercise before continuing.
            if (!hasSelection)
            {
                Toast.makeText(this, R.string.select_at_least_one_exercise, Toast.LENGTH_SHORT).show();
                return;
            }
            showLogging();
        });
    }

    private void showLogging()
    {
        currentScreen = LOGGING;
        backCallback.setEnabled(true);
        showScreen(R.layout.screen_workout_logging);

        // Show input cards only for the exercises the user picked.
        for (int i = 0; i < rowIds.length; i++)
        {
            View row = findViewById(rowIds[i]);
            row.setVisibility(selected[i] ? View.VISIBLE : View.GONE);
            ((TextView) row.findViewById(R.id.exerciseName)).setText(exerciseNameIds[i]);
            setInput(row, R.id.inputSets, entries[i][0]);
            setInput(row, R.id.inputReps, entries[i][1]);
            setInput(row, R.id.inputWeight, entries[i][2]);
        }

        findViewById(R.id.loggingBackButton).setOnClickListener(view -> {
            saveEntries();
            showSelection();
        });
        findViewById(R.id.finishWorkoutButton).setOnClickListener(view -> finishWorkout());
    }

    private void finishWorkout()
    {
        saveEntries();
        StringBuilder summary = new StringBuilder();

        // Checks the workout details before showing the summary
        for (int i = 0; i < selected.length; i++)
        {
            if (!selected[i])
            {
                continue;
            }

            // Error check - sets and reps must be whole numbers above zero
            if (!isPositiveInteger(entries[i][0]) || !isPositiveInteger(entries[i][1]))
            {
                Toast.makeText(this, R.string.invalid_positive_number, Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(entries[i][2]))
            {
                summary.append(getString(R.string.summary_details_without_weight,
                        getString(exerciseNameIds[i]),
                        Integer.parseInt(entries[i][0]), Integer.parseInt(entries[i][1])));
            }

            else
            {
                Double weight = parseWeight(entries[i][2]);
                // Error check - entered weight must be a valid number, 0 or more
                if (weight == null)
                {
                    Toast.makeText(this, R.string.invalid_weight, Toast.LENGTH_SHORT).show();
                    return;
                }
                summary.append(getString(R.string.summary_details, getString(exerciseNameIds[i]),
                        Integer.parseInt(entries[i][0]), Integer.parseInt(entries[i][1]), weight));
            }

            summary.append('\n');
        }

        currentScreen = SUMMARY;
        backCallback.setEnabled(true);
        showScreen(R.layout.screen_workout_summary);
        String date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());
        ((TextView) findViewById(R.id.workoutDateText)).setText(getString(R.string.workout_date, date));
        ((TextView) findViewById(R.id.workoutSummaryText)).setText(summary.toString().trim());

        // Save this workout so it shows up in the History tab
        historyDates.add(date);
        historyDetails.add(summary.toString().trim());
        findViewById(R.id.logAnotherWorkoutButton).setOnClickListener(view -> resetWorkout());
    }

    private void saveEntries()
    {
        // Keep the entered values when the user leaves this screen
        for (int i = 0; i < rowIds.length; i++)
        {
            if (!selected[i])
            {
                continue;
            }

            View row = findViewById(rowIds[i]);
            entries[i][0] = readInput(row, R.id.inputSets);
            entries[i][1] = readInput(row, R.id.inputReps);
            entries[i][2] = readInput(row, R.id.inputWeight);
        }
    }

    private void resetWorkout()
    {
        for (int i = 0; i < selected.length; i++)
        {
            selected[i] = false;
            entries[i] = new String[3];
        }

        showStart();
    }

    // Show the requested screen in the main content area.
    private void showScreen(int layoutId)
    {
        ViewGroup container = findViewById(R.id.fragmentContainer);
        container.setVisibility(View.VISIBLE);
        container.removeAllViews();
        getLayoutInflater().inflate(layoutId, container, true);
    }

    // Put a saved value back into its input field.
    private void setInput(View row, int inputId, String value)
    {
        ((EditText) row.findViewById(inputId)).setText(value == null ? "" : value);
    }

    // Get the text the user entered, without extra spaces around it.
    private String readInput(View row, int inputId)
    {
        return ((EditText) row.findViewById(inputId)).getText().toString().trim();
    }

    // Check that sets or reps are whole numbers greater than zero.
    private boolean isPositiveInteger(String value)
    {
        if (TextUtils.isEmpty(value))
        {
            return false;
        }

        try
        {
            return Integer.parseInt(value) > 0;
        }

        catch (NumberFormatException exception)
        {
            return false;
        }
    }

    // Read the weight as a number and returns null if it is blank or invalid.
    private Double parseWeight(String value)
    {
        if (TextUtils.isEmpty(value))
        {
            return null;
        }

        try
        {
            double weight = Double.parseDouble(value);
            return Double.isFinite(weight) && weight >= 0 ? weight : null;
        }

        catch (NumberFormatException exception)
        {
            return null;
        }
    }
}
