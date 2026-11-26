
package com.bondhubazar.categorycard.bondhucategorycard;

// ==========================================
// Android Imports
// ==========================================

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// ==========================================
// App Inventor Imports
// ==========================================

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

// ==========================================
// Extension Designer
// ==========================================

@DesignerComponent(
    version = 2,
    description = "Bondhu Bazar Category Card Extension V2",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)

@SimpleObject(external = true)

@UsesPermissions(
    permissionNames =
    "android.permission.INTERNET"
)

public class BondhuCategoryCard
    extends AndroidNonvisibleComponent {

    // ======================================
    // Global Variables
    // ======================================

    private Context context;

    private Handler handler;

    // সব Card সংরক্ষণ
    private ArrayList<View> cardList =
        new ArrayList<View>();

    // Card-এর ID সংরক্ষণ
    private ArrayList<CardInfo> cardInfoList =
        new ArrayList<CardInfo>();

    // Image Request সংরক্ষণ
    private HashMap<ImageView, Future<?>> imageTasks =
        new HashMap<ImageView, Future<?>>();

    // Image Load করার জন্য Thread Pool
    private ExecutorService executor =
        Executors.newFixedThreadPool(3);

    // Card Design Settings
    private int cardRadius = 12;

    private int cardColor = Color.WHITE;

    private int titleColor = Color.BLACK;

    private int imageHeight = 75;

    private int placeholderColor =
        Color.LTGRAY;

    private int rippleColor =
        Color.parseColor("#33000000");

    private float cardElevation = 4f;

    // ======================================
    // Card Information Class
    // ======================================

    private static class CardInfo {

        String id;
        String title;

        CardInfo(String id, String title) {

            this.id = id;
            this.title = title;
        }
    }

    // ======================================
    // Constructor
    // ======================================

    public BondhuCategoryCard(
        ComponentContainer container) {

        super(container.$form());

        context = container.$context();

        handler = new Handler(
            Looper.getMainLooper()
        );
    }

    // ======================================
    // Add Category Card V1 Compatible
    // ======================================

    @SimpleFunction(
        description = "Horizontal Arrangement-এ Category Card তৈরি করে"
    )

    public void AddCategoryCard(
        AndroidViewComponent arrangement,
        String title,
        String imageUrl,
        int width,
        int height
    ) {

        AddCategoryCardInternal(
            arrangement,
            "",
            title,
            imageUrl,
            width,
            height
        );
    }

    // ======================================
    // Add Category Card With ID
    // ======================================

    @SimpleFunction(
        description = "Category ID সহ নতুন Card তৈরি করে"
    )

    public void AddCategoryCardWithId(
        AndroidViewComponent arrangement,
        String categoryId,
        String title,
        String imageUrl,
        int width,
        int height
    ) {

        AddCategoryCardInternal(
            arrangement,
            categoryId,
            title,
            imageUrl,
            width,
            height
        );
    }

    // ======================================
    // Main Card Creation Function
    // ======================================

    private void AddCategoryCardInternal(
        AndroidViewComponent arrangement,
        String categoryId,
        String title,
        String imageUrl,
        int width,
        int height
    ) {

        // Arrangement Null Check
        if (arrangement == null) {

            ErrorOccurred(
                "Arrangement is null"
            );

            return;
        }

        // Size Validation
        if (width <= 0 || height <= 0) {

            ErrorOccurred(
                "Card width and height must be greater than zero"
            );

            return;
        }

        // Parent View খোঁজা
        View target = arrangement.getView();

        LinearLayout parent =
            findLinearLayout(target);

        if (parent == null) {

            ErrorOccurred(
                "LinearLayout not found inside Arrangement"
            );

            return;
        }

        // ==================================
        // Main Card Layout
        // ==================================

        LinearLayout card =
            new LinearLayout(context);

        card.setOrientation(
            LinearLayout.VERTICAL
        );

        card.setGravity(
            Gravity.CENTER
        );

        card.setClickable(true);

        card.setFocusable(true);

        // ==================================
        // Card Background
        // ==================================

        GradientDrawable background =
            new GradientDrawable();

        background.setColor(cardColor);

        background.setCornerRadius(
            dp(cardRadius)
        );

        card.setBackground(background);

        // Elevation
        card.setElevation(
            dp(cardElevation)
        );

        // ==================================
        // Ripple Effect
        // ==================================

        if (Build.VERSION.SDK_INT >= 21) {

            GradientDrawable mask =
                new GradientDrawable();

            mask.setColor(Color.WHITE);

            mask.setCornerRadius(
                dp(cardRadius)
            );

            RippleDrawable ripple =
                new RippleDrawable(
                    ColorStateList.valueOf(rippleColor),
                    null,
                    mask
                );

            card.setForeground(ripple);
        }

        // ==================================
        // Card Layout Parameters
        // ==================================

        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(
                dp(width),
                dp(height)
            );

        cardParams.setMargins(
            dp(5),
            dp(5),
            dp(5),
            dp(5)
        );

        // ==================================
        // Image View
        // ==================================

        final ImageView image =
            new ImageView(context);

        image.setScaleType(
            ImageView.ScaleType.CENTER_CROP
        );

        image.setBackgroundColor(
            placeholderColor
        );

        image.setImageDrawable(null);

        // Card Height অনুযায়ী Image Height
        int actualImageHeight =
            Math.min(
                imageHeight,
                Math.max(1, height - 35)
            );

        LinearLayout.LayoutParams imageParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(actualImageHeight)
            );

        image.setLayoutParams(imageParams);

        // ==================================
        // Title Text
        // ==================================

        TextView text =
            new TextView(context);

        text.setText(
            title == null ? "" : title
        );

        text.setTextColor(titleColor);

        text.setTextSize(14);

        text.setTypeface(
            Typeface.DEFAULT,
            Typeface.BOLD
        );

        text.setGravity(
            Gravity.CENTER
        );

        text.setMaxLines(2);

        text.setPadding(
            dp(3),
            dp(2),
            dp(3),
            dp(2)
        );

        LinearLayout.LayoutParams textParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            );

        text.setLayoutParams(textParams);

        // ==================================
        // Add Views Into Card
        // ==================================

        card.addView(image);

        card.addView(text);

        // ==================================
        // Add Card Into Arrangement
        // ==================================

        try {

            parent.addView(
                card,
                cardParams
            );

        } catch (Exception e) {

            ErrorOccurred(
                "Card add failed: " + e.getMessage()
            );

            return;
        }

        // ==================================
        // Save Card Information
        // ==================================

        final CardInfo info =
            new CardInfo(
                categoryId == null ? "" : categoryId,
                title == null ? "" : title
            );

        cardList.add(card);

        cardInfoList.add(info);

        final int index =
            cardList.size() - 1;

        // ==================================
        // Card Click Listener
        // ==================================

        card.setOnClickListener(
            new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    int currentIndex =
                        cardList.indexOf(view);

                    if (currentIndex < 0 ||
                        currentIndex >= cardInfoList.size()) {

                        return;
                    }

                    CardInfo currentInfo =
                        cardInfoList.get(currentIndex);

                    CategoryClicked(
                        currentInfo.title,
                        currentIndex
                    );

                    CategoryClickedWithId(
                        currentInfo.id,
                        currentInfo.title,
                        currentIndex
                    );
                }
            }
        );

        // ==================================
        // Image Loading
        // ==================================

        loadImage(
            image,
            imageUrl,
            title,
            index
        );

        // ==================================
        // Card Added Event
        // ==================================

        CardAdded(
            title,
            index
        );
    }

    // ======================================
    // Find LinearLayout
    // ======================================

    private LinearLayout findLinearLayout(
        View view
    ) {

        if (view == null) {
            return null;
        }

        if (view instanceof LinearLayout) {

            return (LinearLayout) view;
        }

        if (view instanceof ViewGroup) {

            ViewGroup group =
                (ViewGroup) view;

            for (int i = 0;
                 i < group.getChildCount();
                 i++) {

                LinearLayout result =
                    findLinearLayout(
                        group.getChildAt(i)
                    );

                if (result != null) {

                    return result;
                }
            }
        }

        return null;
    }

    // ======================================
    // Image URL Loader
    // ======================================

    private void loadImage(
        final ImageView image,
        final String imageUrl,
        final String title,
        final int index
    ) {

        if (imageUrl == null ||
            imageUrl.trim().isEmpty()) {

            ImageLoaded(
                title,
                index,
                false,
                "Image URL is empty"
            );

            return;
        }

        final String urlString =
            imageUrl.trim();

        // HTTP বা HTTPS ছাড়া অন্য URL নয়
        if (!urlString.startsWith("https://") &&
            !urlString.startsWith("http://")) {

            ImageLoaded(
                title,
                index,
                false,
                "Invalid image URL"
            );

            return;
        }

        // নতুন Request Token
        final long requestId =
            System.nanoTime();

        image.setTag(
            Long.valueOf(requestId)
        );

        Future<?> oldTask =
            imageTasks.remove(image);

        if (oldTask != null) {

            oldTask.cancel(true);
        }

        // Background Thread
        Future<?> task =
            executor.submit(new Runnable() {

                @Override
                public void run() {

                    Bitmap bitmap = null;

                    String error = "";

                    HttpURLConnection connection = null;

                    InputStream input = null;

                    try {

                        URL url =
                            new URL(urlString);

                        connection =
                            (HttpURLConnection)
                            url.openConnection();

                        connection.setConnectTimeout(
                            10000
                        );

                        connection.setReadTimeout(
                            15000
                        );

                        connection.setRequestMethod(
                            "GET"
                        );

                        connection.setInstanceFollowRedirects(
                            true
                        );

                        connection.connect();

                        int responseCode =
                            connection.getResponseCode();

                        if (responseCode !=
                            HttpURLConnection.HTTP_OK) {

                            throw new Exception(
                                "HTTP Error: " + responseCode
                            );
                        }

                        input =
                            connection.getInputStream();

                        bitmap =
                            BitmapFactory.decodeStream(
                                input
                            );

                        if (bitmap == null) {

                            throw new Exception(
                                "Invalid image file"
                            );
                        }

                    } catch (Exception e) {

                        error = e.getMessage();

                        if (error == null) {
                            error = "Image loading failed";
                        }

                    } finally {

                        try {

                            if (input != null) {
                                input.close();
                            }

                        } catch (Exception ignored) {
                        }

                        if (connection != null) {
                            connection.disconnect();
                        }
                    }

                    final Bitmap finalBitmap =
                        bitmap;

                    final String finalError =
                        error;

                    // Main Thread-এ UI Update
                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            Object currentTag =
                                image.getTag();

                            if (!(currentTag instanceof Long) ||
                                ((Long) currentTag).longValue()
                                != requestId) {

                                return;
                            }

                            if (image.getParent() == null) {
                                return;
                            }

                            imageTasks.remove(image);

                            if (finalBitmap != null) {

                                image.setImageBitmap(
                                    finalBitmap
                                );

                                ImageLoaded(
                                    title,
                                    index,
                                    true,
                                    "Image loaded successfully"
                                );

                            } else {

                                image.setImageDrawable(
                                    null
                                );

                                image.setBackgroundColor(
                                    placeholderColor
                                );

                                ImageLoaded(
                                    title,
                                    index,
                                    false,
                                    finalError
                                );
                            }
                        }
                    });
                }
            });

        imageTasks.put(
            image,
            task
        );
    }

    // ======================================
    // Set Card Radius
    // ======================================

    @SimpleFunction
    public void SetCornerRadius(int radius) {

        cardRadius = Math.max(0, radius);
    }

    // ======================================
    // Set Card Color
    // ======================================

    @SimpleFunction
    public void SetCardColor(int color) {

        cardColor = color;
    }

    // ======================================
    // Set Title Color
    // ======================================

    @SimpleFunction
    public void SetTitleColor(int color) {

        titleColor = color;
    }

    // ======================================
    // Set Image Height
    // ======================================

    @SimpleFunction
    public void SetImageHeight(int height) {

        imageHeight = Math.max(1, height);
    }

    // ======================================
    // Set Placeholder Color
    // ======================================

    @SimpleFunction
    public void SetPlaceholderColor(int color) {

        placeholderColor = color;
    }

    // ======================================
    // Set Ripple Color
    // ======================================

    @SimpleFunction
    public void SetRippleColor(int color) {

        rippleColor = color;
    }

    // ======================================
    // Set Card Elevation
    // ======================================

    @SimpleFunction
    public void SetCardElevation(float elevation) {

        cardElevation = Math.max(0f, elevation);
    }

    // ======================================
    // Get Card Count
    // ======================================

    @SimpleFunction
    public int GetCardCount() {

        return cardList.size();
    }

    // ======================================
    // Get Category ID
    // ======================================

    @SimpleFunction
    public String GetCategoryId(int index) {

        if (index < 0 ||
            index >= cardInfoList.size()) {

            return "";
        }

        return cardInfoList.get(index).id;
    }

    // ======================================
    // Get Category Title
    // ======================================

    @SimpleFunction
    public String GetCategoryTitle(int index) {

        if (index < 0 ||
            index >= cardInfoList.size()) {

            return "";
        }

        return cardInfoList.get(index).title;
    }

    // ======================================
    // Cancel Image Requests
    // ======================================

    @SimpleFunction
    public void CancelAllImageLoads() {

        for (Future<?> task : imageTasks.values()) {

            if (task != null) {
                task.cancel(true);
            }
        }

        imageTasks.clear();
    }

    // ======================================
    // Clear All Cards
    // ======================================

    @SimpleFunction
    public void ClearCards() {

        // Image Request Cancel
        CancelAllImageLoads();

        // সব Card Remove
        for (View card : cardList) {

            if (card.getParent()
                instanceof ViewGroup) {

                ViewGroup parent =
                    (ViewGroup) card.getParent();

                parent.removeView(card);
            }
        }

        cardList.clear();

        cardInfoList.clear();

        CardsCleared();
    }

    // ======================================
    // DP Conversion
    // ======================================

    private int dp(float value) {

        return (int) (
            value *
            context.getResources()
            .getDisplayMetrics()
            .density + 0.5f
        );
    }

    // ======================================
    // EVENTS
    // ======================================

    @SimpleEvent
    public void CategoryClicked(
        String title,
        int index
    ) {

        EventDispatcher.dispatchEvent(
            this,
            "CategoryClicked",
            title,
            index
        );
    }

    @SimpleEvent
    public void CategoryClickedWithId(
        String categoryId,
        String title,
        int index
    ) {

        EventDispatcher.dispatchEvent(
            this,
            "CategoryClickedWithId",
            categoryId,
            title,
            index
        );
    }

    @SimpleEvent
    public void CardAdded(
        String title,
        int index
    ) {

        EventDispatcher.dispatchEvent(
            this,
            "CardAdded",
            title,
            index
        );
    }

    @SimpleEvent
    public void ImageLoaded(
        String title,
        int index,
        boolean success,
        String message
    ) {

        EventDispatcher.dispatchEvent(
            this,
            "ImageLoaded",
            title,
            index,
            success,
            message
        );
    }

    @SimpleEvent
    public void CardsCleared() {

        EventDispatcher.dispatchEvent(
            this,
            "CardsCleared"
        );
    }

    @SimpleEvent
    public void ErrorOccurred(String message) {

        EventDispatcher.dispatchEvent(
            this,
            "ErrorOccurred",
            message
        );
    }
}
