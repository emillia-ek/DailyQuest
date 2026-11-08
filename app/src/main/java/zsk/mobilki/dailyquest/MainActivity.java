package zsk.mobilki.dailyquest;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import zsk.mobilki.dailyquest.data.local.DebugSeedData;
import zsk.mobilki.dailyquest.ui.statistics.StatisticsFragment;
import zsk.mobilki.dailyquest.ui.history.FocusHistoryFragment;
import zsk.mobilki.dailyquest.ui.home.HomeFragment;
import zsk.mobilki.dailyquest.ui.profile.ProfileFragment;
import zsk.mobilki.dailyquest.ui.tasklist.TaskListFragment;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome;
    private LinearLayout navTasks;
    private LinearLayout navStatistics;
    private LinearLayout navHistory;
    private LinearLayout navProfile;

    private ImageView iconHome;
    private ImageView iconTasks;
    private ImageView iconStatistics;
    private ImageView iconHistory;
    private ImageView iconProfile;

    private TextView labelHome;
    private TextView labelTasks;
    private TextView labelStatistics;
    private TextView labelHistory;
    private TextView labelProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            DebugSeedData.seedIfNeeded(this);
        }

        bindNavigation();
        setupNavigation();

        if (savedInstanceState == null) {
            navigateToTab(R.id.nav_home);
        }
    }

    private void bindNavigation() {
        navHome = findViewById(R.id.nav_home);
        navTasks = findViewById(R.id.nav_tasks);
        navStatistics = findViewById(R.id.nav_statistics);
        navHistory = findViewById(R.id.nav_history);
        navProfile = findViewById(R.id.nav_profile);

        iconHome = findViewById(R.id.nav_home_icon);
        iconTasks = findViewById(R.id.nav_tasks_icon);
        iconStatistics = findViewById(R.id.nav_statistics_icon);
        iconHistory = findViewById(R.id.nav_history_icon);
        iconProfile = findViewById(R.id.nav_profile_icon);

        labelHome = findViewById(R.id.nav_home_label);
        labelTasks = findViewById(R.id.nav_tasks_label);
        labelStatistics = findViewById(R.id.nav_statistics_label);
        labelHistory = findViewById(R.id.nav_history_label);
        labelProfile = findViewById(R.id.nav_profile_label);
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> navigateToTab(R.id.nav_home));
        navTasks.setOnClickListener(v -> navigateToTab(R.id.nav_tasks));
        navStatistics.setOnClickListener(v -> navigateToTab(R.id.nav_statistics));
        navHistory.setOnClickListener(v -> navigateToTab(R.id.nav_history));
        navProfile.setOnClickListener(v -> navigateToTab(R.id.nav_profile));
    }

    public void navigateToTab(int menuItemId) {
        Fragment selectedFragment = null;

        if (menuItemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (menuItemId == R.id.nav_tasks) {
            selectedFragment = new TaskListFragment();
        } else if (menuItemId == R.id.nav_statistics) {
            selectedFragment = new StatisticsFragment();
        } else if (menuItemId == R.id.nav_history) {
            selectedFragment = new FocusHistoryFragment();
        } else if (menuItemId == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            setSelectedNavigationItem(menuItemId);
        }
    }

    private void setSelectedNavigationItem(int selectedId) {
        int activeColor = ContextCompat.getColor(this, R.color.cobalt_blue);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_text);

        updateNavigationItem(iconHome, labelHome, selectedId == R.id.nav_home, activeColor, inactiveColor);
        updateNavigationItem(iconTasks, labelTasks, selectedId == R.id.nav_tasks, activeColor, inactiveColor);
        updateNavigationItem(iconStatistics, labelStatistics, selectedId == R.id.nav_statistics, activeColor, inactiveColor);
        updateNavigationItem(iconHistory, labelHistory, selectedId == R.id.nav_history, activeColor, inactiveColor);
        updateNavigationItem(iconProfile, labelProfile, selectedId == R.id.nav_profile, activeColor, inactiveColor);
    }

    private void updateNavigationItem(ImageView icon, TextView label, boolean selected, int activeColor, int inactiveColor) {
        icon.setColorFilter(selected ? activeColor : inactiveColor);
        label.setTextColor(selected ? activeColor : inactiveColor);
    }
}
