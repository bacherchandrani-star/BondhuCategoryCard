
package com.bondhubazar.categorycard.bondhucategorycard;

// Android Context
import android.content.Context;

// Android View
import android.view.View;
import android.view.ViewGroup;

// LinearLayout
import android.widget.LinearLayout;

// ImageView
import android.widget.ImageView;

// TextView
import android.widget.TextView;

// Background Shape
import android.graphics.drawable.GradientDrawable;

// Ripple Effect
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;

// Android Color
import android.graphics.Color;

// Image Bitmap
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

// Internet Connection
import java.net.URL;
import java.net.HttpURLConnection;

// Collections
import java.util.HashMap;
import java.util.Map;

// App Inventor Extension
import com.google.appinventor.components.annotations.Extension;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

@Extension(
    description = "Bondhu Bazar Category Card Extension"
)

public class BondhuCategoryCard
        extends AndroidNonvisibleComponent {

    // Android Context
    private Context context;

    // Category Card রাখার Map
    private Map<String, LinearLayout> cards = new HashMap<>();

    // Category Image রাখার Map
    private Map<String, ImageView> images = new HashMap<>();

    // Category Name রাখার Map
    private Map<String, TextView> titles = new HashMap<>();

    // Category Card-এর Background রাখার Map
    private Map<String, GradientDrawable> backgrounds = new HashMap<>();

    // Constructor
    public BondhuCategoryCard(ComponentContainer container) {

        super(container.$form());

        // Context নেওয়া
        context = container.$context();
    }

    // =========================================
    // ১. Category Card তৈরি করা
    // =========================================

    @SimpleFunction(
        description = "Category Card তৈরি করুন"
    )
    public void CreateCategoryCard(
            ComponentContainer parent,
            String categoryId,
            String categoryName,
            int width,
            int height
    ) {

        try {

            // একই ID আগে থাকলে পুরনো Card সরানো
            RemoveCategory(categoryId);

            // Main Card তৈরি
            LinearLayout card = new LinearLayout(context);

            // Card Vertical Layout
            card.setOrientation(LinearLayout.VERTICAL);

            // Card Center করা
            card.setGravity(android.view.Gravity.CENTER);

            // Padding
            card.setPadding(
                    dp(8),
                    dp(8),
                    dp(8),
                    dp(8)
            );

            // Card Background
            GradientDrawable background = new GradientDrawable();

            // Background Color
            background.setColor(Color.WHITE);

            // Corner Radius
            background.setCornerRadius(dp(14));

            // Background Apply
            card.setBackground(background);

            // Card Click Enable
            card.setClickable(true);

            // Card-এর Shadow
            card.setElevation(dp(3));

            // Ripple Effect
            RippleDrawable ripple = new RippleDrawable(
                    ColorStateList.valueOf(
                            Color.parseColor("#33000000")
                    ),
                    background,
                    null
            );

            card.setBackground(ripple);

            // ImageView তৈরি
            ImageView image = new ImageView(context);

            // Image Center
            image.setScaleType(
                    ImageView.ScaleType.CENTER_CROP
            );

            // Image Size
            LinearLayout.LayoutParams imageParams =
                    new LinearLayout.LayoutParams(
                            dp(65),
                            dp(65)
                    );

            // ImageView-এর Layout
            image.setLayoutParams(imageParams);

            // TextView তৈরি
            TextView title = new TextView(context);

            // Category Name
            title.setText(categoryName);

            // Text Color
            title.setTextColor(Color.parseColor("#222222"));

            // Text Size
            title.setTextSize(14);

            // Bold Text
            title.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
            );

            // Text Center
            title.setGravity(
                    android.view.Gravity.CENTER
            );

            // Image এবং Text-এর Gap
            title.setPadding(
                    0,
                    dp(7),
                    0,
                    0
            );

            // Text Layout
            LinearLayout.LayoutParams titleParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            // Text-এর Layout Apply
            title.setLayoutParams(titleParams);

            // Image Card-এ যোগ
            card.addView(image);

            // Text Card-এ যোগ
            card.addView(title);

            // Card Width এবং Height
            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            dp(width),
                            dp(height)
                    );

            // Card-এর চারপাশে Gap
            cardParams.setMargins(
                    dp(5),
                    dp(5),
                    dp(5),
                    dp(5)
            );

            // Card Layout Apply
            card.setLayoutParams(cardParams);

            // Parent Layout-এ Card যোগ
            parent.getView().addView(card);

            // Map-এ Card Save
            cards.put(categoryId, card);

            // Map-এ Image Save
            images.put(categoryId, image);

            // Map-এ Title Save
            titles.put(categoryId, title);

            // Map-এ Background Save
            backgrounds.put(categoryId, background);

            // Click Listener
            card.setOnClickListener(
                    new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    // Category Click Event
                    CategoryClicked(
                            categoryId,
                            categoryName
                    );
                }
            });

        } catch (Exception e) {

            // Error Event
            ErrorOccurred(
                    "CreateCategoryCard: " + e.getMessage()
            );
        }
    }

    // =========================================
    // ২. Category Name পরিবর্তন
    // =========================================

    @SimpleFunction(
        description = "Category Card-এর নাম পরিবর্তন করুন"
    )
    public void SetCategoryName(
            String categoryId,
            String categoryName
    ) {

        TextView title = titles.get(categoryId);

        if (title != null) {

            title.setText(categoryName);
        }
    }

    // =========================================
    // ৩. Category Image URL সেট করা
    // =========================================

    @SimpleFunction(
        description = "Category Card-এ Internet Image URL সেট করুন"
    )
    public void SetCategoryImage(
            String categoryId,
            String imageUrl
    ) {

        final ImageView image = images.get(categoryId);

        if (image == null) {
            return;
        }

        // Background Thread-এ Image Download
        new Thread(
                new Runnable() {

            @Override
            public void run() {

                HttpURLConnection connection = null;

                try {

                    // URL তৈরি
                    URL url = new URL(imageUrl);

                    // Connection তৈরি
                    connection =
                            (HttpURLConnection) url.openConnection();

                    // Timeout
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    // Request Method
                    connection.setRequestMethod("GET");

                    // Internet থেকে Image নেওয়া
                    connection.connect();

                    // Image Stream
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            connection.getInputStream()
                    );

                    // Image পাওয়া গেলে
                    if (bitmap != null) {

                        // Main UI Thread-এ Image সেট
                        image.post(
                                new Runnable() {

                            @Override
                            public void run() {

                                image.setImageBitmap(bitmap);
                            }
                        });
                    }

                } catch (Exception e) {

                    // Image Error
                    ErrorOccurred(
                            "SetCategoryImage: " + e.getMessage()
                    );

                } finally {

                    // Connection বন্ধ
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

        }).start();
    }

    // =========================================
    // ৪. Background Color পরিবর্তন
    // =========================================

    @SimpleFunction(
        description = "Category Card-এর Background Color পরিবর্তন করুন"
    )
    public void SetCategoryBackgroundColor(
            String categoryId,
            String colorCode
    ) {

        GradientDrawable background =
                backgrounds.get(categoryId);

        if (background != null) {

            try {

                background.setColor(
                        Color.parseColor(colorCode)
                );

            } catch (Exception e) {

                ErrorOccurred(
                        "SetCategoryBackgroundColor: "
                        + e.getMessage()
                );
            }
        }
    }

    // =========================================
    // ৫. Corner Radius পরিবর্তন
    // =========================================

    @SimpleFunction(
        description = "Category Card-এর Corner Radius পরিবর্তন করুন"
    )
    public void SetCategoryRadius(
            String categoryId,
            int radius
    ) {

        GradientDrawable background =
                backgrounds.get(categoryId);

        if (background != null) {

            background.setCornerRadius(dp(radius));
        }
    }

    // =========================================
    // ৬. Image Size পরিবর্তন
    // =========================================

    @SimpleFunction(
        description = "Category Image-এর Width এবং Height পরিবর্তন করুন"
    )
    public void SetCategoryImageSize(
            String categoryId,
            int width,
            int height
    ) {

        ImageView image = images.get(categoryId);

        if (image != null) {

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            dp(width),
                            dp(height)
                    );

            image.setLayoutParams(params);
        }
    }

    // =========================================
    // ৭. Category Card Show/Hide
    // =========================================

    @SimpleFunction(
        description = "Category Card Visible অথবা Invisible করুন"
    )
    public void SetCategoryVisible(
            String categoryId,
            boolean visible
    ) {

        LinearLayout card = cards.get(categoryId);

        if (card != null) {

            if (visible) {

                card.setVisibility(View.VISIBLE);

            } else {

                card.setVisibility(View.GONE);
            }
        }
    }

    // =========================================
    // ৮. একটি Category Card Remove করা
    // =========================================

    @SimpleFunction(
        description = "Category Card মুছে ফেলুন"
    )
    public void RemoveCategory(
            String categoryId
    ) {

        LinearLayout card = cards.get(categoryId);

        if (card != null) {

            // Parent থেকে Card সরানো
            if (card.getParent() instanceof ViewGroup) {

                ((ViewGroup) card.getParent())
                        .removeView(card);
            }

            // Map থেকে সরানো
            cards.remove(categoryId);
            images.remove(categoryId);
            titles.remove(categoryId);
            backgrounds.remove(categoryId);
        }
    }

    // =========================================
    // ৯. সমস্ত Category Remove করা
    // =========================================

    @SimpleFunction(
        description = "সমস্ত Category Card Remove করুন"
    )
    public void RemoveAllCategories() {

        // Card ID-এর List
        java.util.ArrayList<String> ids =
                new java.util.ArrayList<>(cards.keySet());

        // প্রতিটি Card Remove
        for (String id : ids) {

            RemoveCategory(id);
        }
    }

    // =========================================
    // ১০. Category Click Event
    // =========================================

    @SimpleEvent(
        description = "Category Card Click করলে Event চালু হবে"
    )
    public void CategoryClicked(
            String categoryId,
            String categoryName
    ) {

        EventDispatcher.dispatchEvent(
                this,
                "CategoryClicked",
                categoryId,
                categoryName
        );
    }

    // =========================================
    // ১১. Error Event
    // =========================================

    @SimpleEvent(
        description = "Extension-এর কোনো সমস্যা হলে Event চালু হবে"
    )
    public void ErrorOccurred(
            String message
    ) {

        EventDispatcher.dispatchEvent(
                this,
                "ErrorOccurred",
                message
        );
    }

    // =========================================
    // ১২. DP থেকে Pixel-এ রূপান্তর
    // =========================================

    private int dp(int value) {

        return (int) (
                value * context.getResources()
                        .getDisplayMetrics()
                        .density + 0.5f
        );
    }
}
