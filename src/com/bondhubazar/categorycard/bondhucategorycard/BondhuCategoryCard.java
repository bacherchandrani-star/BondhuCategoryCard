
package com.bondhubazar.categorycard.bondhucategorycard;

// Android imports
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

// App Inventor imports
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

@Extension
@DesignerComponent(
    version = 2,
    description = "Bondhu Bazar Category Card Extension V2",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(
    permissionNames = "android.permission.INTERNET"
)

public class BondhuCategoryCard
    extends AndroidNonvisibleComponent {

    private Context context;
    private Handler handler;

    private ArrayList<View> cardList =
        new ArrayList<View>();

    private ArrayList<CardInfo> cardInfoList =
        new ArrayList<CardInfo>();

    private HashMap<ImageView, Future<?>> imageTasks =
        new HashMap<ImageView, Future<?>>();

    private ExecutorService executor =
        Executors.newFixedThreadPool(3);

    private int cardRadius = 12;
    private int cardColor = Color.WHITE;
    private int titleColor = Color.BLACK;
    private int imageHeight = 75;
    private int placeholderColor = Color.LTGRAY;
    private int rippleColor = Color.parseColor("#33000000");
    private float cardElevation = 4f;

    private static class CardInfo {
        String id;
        String title;

        CardInfo(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }

    public BondhuCategoryCard(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        handler = new Handler(Looper.getMainLooper());
    }

    @SimpleFunction(
        description = "Create category card in Horizontal Arrangement"
    )
    public void AddCategoryCard(
        AndroidViewComponent arrangement,
        String title,
        String imageUrl,
        int width,
        int height
    ) {
        AddCategoryCardInternal(
            arrangement, "", title, imageUrl, width, height
        );
    }

    @SimpleFunction(
        description = "Create category card with category ID"
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
            arrangement, categoryId, title, imageUrl, width, height
        );
    }

    private void AddCategoryCardInternal(
        AndroidViewComponent arrangement,
        String categoryId,
        String title,
        String imageUrl,
        int width,
        int height
    ) {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() ->
                AddCategoryCardInternal(
                    arrangement, categoryId, title,
                    imageUrl, width, height
                )
            );
            return;
        }

        if (arrangement == null) {
            ErrorOccurred("Arrangement is null");
            return;
        }

        if (width <= 0 || height <= 0) {
            ErrorOccurred("Invalid card size");
            return;
        }

        LinearLayout parent =
            findLinearLayout(arrangement.getView());

        if (parent == null) {
            ErrorOccurred("LinearLayout not found");
            return;
        }

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setClickable(true);
        card.setFocusable(true);

        GradientDrawable background = new GradientDrawable();
        background.setColor(cardColor);
        background.setCornerRadius(dp(cardRadius));
        card.setBackground(background);

        if (Build.VERSION.SDK_INT >= 21) {
            card.setElevation(dp(cardElevation));

            GradientDrawable mask = new GradientDrawable();
            mask.setColor(Color.WHITE);
            mask.setCornerRadius(dp(cardRadius));

            card.setForeground(new RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                null,
                mask
            ));
        }

        LinearLayout.LayoutParams cardParams =
            new LinearLayout.LayoutParams(dp(width), dp(height));

        cardParams.setMargins(dp(5), dp(5), dp(5), dp(5));

        ImageView image = new ImageView(context);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundColor(placeholderColor);

        int actualImageHeight =
            Math.min(imageHeight, Math.max(1, height - 35));

        LinearLayout.LayoutParams imageParams =
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(actualImageHeight)
            );

        image.setLayoutParams(imageParams);

        TextView text = new TextView(context);
        text.setText(title == null ? "" : title);
        text.setTextColor(titleColor);
        text.setTextSize(14);
        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.setGravity(Gravity.CENTER);
        text.setMaxLines(2);
        text.setPadding(dp(3), dp(2), dp(3), dp(2));

        text.setLayoutParams(
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        );

        card.addView(image);
        card.addView(text);

        try {
            parent.addView(card, cardParams);
        } catch (Exception e) {
            ErrorOccurred("Card add failed: " + e.getMessage());
            return;
        }

        final CardInfo info = new CardInfo(
            categoryId == null ? "" : categoryId,
            title == null ? "" : title
        );

        cardList.add(card);
        cardInfoList.add(info);

        final int index = cardList.size() - 1;

        card.setOnClickListener(view -> {
            int currentIndex = cardList.indexOf(view);

            if (currentIndex < 0 ||
                currentIndex >= cardInfoList.size()) {
                return;
            }

            CardInfo current = cardInfoList.get(currentIndex);

            CategoryClicked(current.title, currentIndex);

            CategoryClickedWithId(
                current.id, current.title, currentIndex
            );
        });

        loadImage(image, imageUrl, title, index);

        CardAdded(title, index);
    }

    private LinearLayout findLinearLayout(View view) {
        if (view == null) return null;

        if (view instanceof LinearLayout) {
            return (LinearLayout) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                LinearLayout result =
                    findLinearLayout(group.getChildAt(i));

                if (result != null) return result;
            }
        }

        return null;
    }

    private void loadImage(
        final ImageView image,
        final String imageUrl,
        final String title,
        final int index
    ) {

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            ImageLoaded(title, index, false, "Image URL is empty");
            return;
        }

        final String urlString = imageUrl.trim();

        if (!urlString.startsWith("https://") &&
            !urlString.startsWith("http://")) {
            ImageLoaded(title, index, false, "Invalid image URL");
            return;
        }

        final long requestId = System.nanoTime();
        image.setTag(Long.valueOf(requestId));

        Future<?> oldTask = imageTasks.remove(image);
        if (oldTask != null) oldTask.cancel(true);

        Future<?> task = executor.submit(() -> {

            Bitmap bitmap = null;
            String error = "";
            HttpURLConnection connection = null;
            InputStream input = null;

            try {
                URL url = new URL(urlString);

                connection =
                    (HttpURLConnection) url.openConnection();

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(15000);
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP Error: " + responseCode);
                }

                input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);

                if (bitmap == null) {
                    throw new Exception("Invalid image file");
                }

            } catch (Exception e) {
                error = e.getMessage();
                if (error == null) error = "Image loading failed";

            } finally {
                try {
                    if (input != null) input.close();
                } catch (Exception ignored) {}

                if (connection != null) connection.disconnect();
            }

            final Bitmap resultBitmap = bitmap;
            final String resultError = error;

            handler.post(() -> {

                Object tag = image.getTag();

                if (!(tag instanceof Long) ||
                    ((Long) tag).longValue() != requestId) {
                    return;
                }

                if (image.getParent() == null) return;

                imageTasks.remove(image);

                if (resultBitmap != null) {
                    image.setImageBitmap(resultBitmap);
                    ImageLoaded(title, index, true, "Image loaded");
                } else {
                    image.setImageDrawable(null);
                    image.setBackgroundColor(placeholderColor);
                    ImageLoaded(title, index, false, resultError);
                }
            });
        });

        imageTasks.put(image, task);
    }

    @SimpleFunction
    public void SetCornerRadius(int radius) {
        cardRadius = Math.max(0, radius);
    }

    @SimpleFunction
    public void SetCardColor(int color) {
        cardColor = color;
    }

    @SimpleFunction
    public void SetTitleColor(int color) {
        titleColor = color;
    }

    @SimpleFunction
    public void SetImageHeight(int height) {
        imageHeight = Math.max(1, height);
    }

    @SimpleFunction
    public void SetPlaceholderColor(int color) {
        placeholderColor = color;
    }

    @SimpleFunction
    public void SetRippleColor(int color) {
        rippleColor = color;
    }

    @SimpleFunction
    public void SetCardElevation(float elevation) {
        cardElevation = Math.max(0f, elevation);
    }

    @SimpleFunction
    public int GetCardCount() {
        return cardList.size();
    }

    @SimpleFunction
    public String GetCategoryId(int index) {
        if (index < 0 || index >= cardInfoList.size()) return "";
        return cardInfoList.get(index).id;
    }

    @SimpleFunction
    public String GetCategoryTitle(int index) {
        if (index < 0 || index >= cardInfoList.size()) return "";
        return cardInfoList.get(index).title;
    }

    @SimpleFunction
    public void CancelAllImageLoads() {
        for (Future<?> task : imageTasks.values()) {
            if (task != null) task.cancel(true);
        }
        imageTasks.clear();
    }

    @SimpleFunction
    public void ClearCards() {

        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(() -> ClearCards());
            return;
        }

        CancelAllImageLoads();

        for (View card : cardList) {
            if (card.getParent() instanceof ViewGroup) {
                ((ViewGroup) card.getParent()).removeView(card);
            }
        }

        cardList.clear();
        cardInfoList.clear();

        CardsCleared();
    }

    private int dp(float value) {
        return (int) (
            value * context.getResources()
            .getDisplayMetrics().density + 0.5f
        );
    }

    @SimpleEvent
    public void CategoryClicked(String title, int index) {
        EventDispatcher.dispatchEvent(
            this, "CategoryClicked", title, index
        );
    }

    @SimpleEvent
    public void CategoryClickedWithId(
        String categoryId, String title, int index
    ) {
        EventDispatcher.dispatchEvent(
            this, "CategoryClickedWithId",
            categoryId, title, index
        );
    }

    @SimpleEvent
    public void CardAdded(String title, int index) {
        EventDispatcher.dispatchEvent(
            this, "CardAdded", title, index
        );
    }

    @SimpleEvent
    public void ImageLoaded(
        String title, int index,
        boolean success, String message
    ) {
        EventDispatcher.dispatchEvent(
            this, "ImageLoaded",
            title, index, success, message
        );
    }

    @SimpleEvent
    public void CardsCleared() {
        EventDispatcher.dispatchEvent(
            this, "CardsCleared"
        );
    }

    @SimpleEvent
    public void ErrorOccurred(String message) {
        EventDispatcher.dispatchEvent(
            this, "ErrorOccurred", message
        );
    }
}
