package zsk.mobilki.dailyquest.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import zsk.mobilki.dailyquest.R;
import zsk.mobilki.dailyquest.data.repository.QuestRepository;

public class ProfileFragment extends Fragment {

    private TextView textLevel;
    private TextView textXp;
    private TextView textStreak;
    private TextView textCompleted;
    private TextView textCoinsBalance;
    private TextView textEquippedAccessory;
    private ImageView imageAccessory;
    private LinearLayout layoutAccessoryShop;
    private Button buttonReset;

    private QuestRepository repository;
    private String previewAccessoryId = "";

    private static class AccessoryItem {
        final String id;
        final String name;
        final int price;
        final int imageRes;

        AccessoryItem(String id, String name, int price, int imageRes) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.imageRes = imageRes;
        }
    }

    private final AccessoryItem[] accessories = {
            new AccessoryItem("bow", "Bow Tie", 30, R.drawable.accessory_bow),
            new AccessoryItem("glasses", "Natural", 50, R.drawable.accessory_natural),
            new AccessoryItem("smile", "Smart Smile", 70, R.drawable.accessory_smile)
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        repository = new QuestRepository(requireContext());

        textLevel = view.findViewById(R.id.profile_text_level);
        textXp = view.findViewById(R.id.profile_text_xp);
        textStreak = view.findViewById(R.id.profile_text_streak);
        textCompleted = view.findViewById(R.id.profile_text_completed);
        textCoinsBalance = view.findViewById(R.id.profile_text_coins_balance);
        textEquippedAccessory = view.findViewById(R.id.profile_text_equipped_accessory);
        imageAccessory = view.findViewById(R.id.profile_image_accessory);
        layoutAccessoryShop = view.findViewById(R.id.profile_layout_accessory_shop);
        buttonReset = view.findViewById(R.id.button_reset_profile);

        updateUi();

        buttonReset.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                    .setTitle("Reset progress?")
                    .setMessage("This will clear your level, XP, streak, tasks, and focus history. This action cannot be undone.")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        repository.resetAllData();
                        repository = new QuestRepository(requireContext());
                        previewAccessoryId = "";
                        
                        Toast.makeText(getContext(), "Progress reset.", Toast.LENGTH_SHORT).show();
                        updateUi();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void updateUi() {
        int level = repository.getLevel();
        int totalXp = repository.getTotalXp();
        int streak = repository.getStreak();
        int completedCount = repository.getCompletedTasks().size();
        int coins = repository.getCoins();

        textLevel.setText(String.valueOf(level));
        textXp.setText(totalXp + " XP");
        textStreak.setText(streak + (streak == 1 ? " day" : " days"));
        textCompleted.setText(completedCount + (completedCount == 1 ? " task" : " tasks"));
        textCoinsBalance.setText(coins + (coins == 1 ? " coin" : " coins"));
        updateAvatarAccessory();
        renderAccessoryShop();
    }

    private void updateAvatarAccessory() {
        String accessoryId = previewAccessoryId.isEmpty() ? repository.getEquippedAccessory() : previewAccessoryId;
        AccessoryItem accessory = findAccessory(accessoryId);
        if (accessory == null) {
            imageAccessory.setVisibility(View.GONE);
            textEquippedAccessory.setText("No accessory equipped");
            return;
        }
        imageAccessory.setImageResource(accessory.imageRes);
        imageAccessory.setVisibility(View.VISIBLE);
        textEquippedAccessory.setText(previewAccessoryId.isEmpty()
                ? "Wearing: " + accessory.name
                : "Previewing: " + accessory.name);
    }

    private void renderAccessoryShop() {
        layoutAccessoryShop.removeAllViews();
        String equippedAccessory = repository.getEquippedAccessory();

        if (!equippedAccessory.isEmpty()) {
            Button unequipButton = makeActionButton("Remove");
            unequipButton.setOnClickListener(v -> {
                previewAccessoryId = "";
                repository.unequipAccessory();
                updateUi();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(44)
            );
            params.bottomMargin = dp(10);
            layoutAccessoryShop.addView(unequipButton, params);
        }

        for (AccessoryItem accessory : accessories) {
            layoutAccessoryShop.addView(makeAccessoryRow(accessory, equippedAccessory));
        }
    }

    private View makeAccessoryRow(AccessoryItem accessory, String equippedAccessory) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.card_outlined);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout info = new LinearLayout(requireContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(12), 0, dp(8), 0);

        TextView name = makeText(accessory.name, 15, R.color.cobalt_blue);
        String statusText = repository.ownsAccessory(accessory.id) ? "Owned" : accessory.price + " coins";
        TextView price = makeText(statusText, 12, R.color.gray_text);
        info.addView(name);
        info.addView(price);
        row.addView(info, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button tryButton = makeActionButton("Preview");
        tryButton.setOnClickListener(v -> {
            previewAccessoryId = accessory.id;
            updateAvatarAccessory();
        });
        row.addView(tryButton, new LinearLayout.LayoutParams(dp(82), dp(42)));

        Button actionButton = makeActionButton(getAccessoryActionLabel(accessory, equippedAccessory));
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(dp(82), dp(42));
        actionParams.leftMargin = dp(8);
        row.addView(actionButton, actionParams);

        if (accessory.id.equals(equippedAccessory)) {
            actionButton.setEnabled(false);
        } else {
            actionButton.setOnClickListener(v -> handleAccessoryAction(accessory));
        }

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(10);
        row.setLayoutParams(rowParams);
        return row;
    }

    private String getAccessoryActionLabel(AccessoryItem accessory, String equippedAccessory) {
        if (accessory.id.equals(equippedAccessory)) {
            return "Wearing";
        }
        return repository.ownsAccessory(accessory.id) ? "Equip" : "Buy";
    }

    private void handleAccessoryAction(AccessoryItem accessory) {
        if (repository.ownsAccessory(accessory.id)) {
            repository.equipAccessory(accessory.id);
            previewAccessoryId = "";
            updateUi();
            return;
        }

        if (repository.buyAccessory(accessory.id, accessory.price)) {
            previewAccessoryId = "";
            Toast.makeText(getContext(), accessory.name + " equipped.", Toast.LENGTH_SHORT).show();
            updateUi();
        } else {
            Toast.makeText(getContext(), "Not enough coins.", Toast.LENGTH_SHORT).show();
        }
    }

    private AccessoryItem findAccessory(String accessoryId) {
        for (AccessoryItem accessory : accessories) {
            if (accessory.id.equals(accessoryId)) {
                return accessory;
            }
        }
        return null;
    }

    private TextView makeText(String text, int sizeSp, int colorRes) {
        TextView textView = new TextView(requireContext());
        textView.setText(text);
        textView.setTextSize(sizeSp);
        textView.setTextColor(getResources().getColor(colorRes, null));
        textView.setTypeface(getResources().getFont(R.font.outfit));
        return textView;
    }

    private Button makeActionButton(String text) {
        Button button = new Button(requireContext());
        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(getResources().getColor(R.color.cobalt_blue, null));
        button.setTypeface(getResources().getFont(R.font.outfit));
        button.setBackgroundResource(R.drawable.border_button_cream);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setSingleLine(true);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setPadding(dp(8), 0, dp(8), 0);
        return button;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUi();
    }
}
